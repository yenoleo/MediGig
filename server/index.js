import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';
import db, {
  persist, resetDb, hashPassword, verifyPassword, id, token, isoNow,
  addBusinessDays, cityCoords, haversineKm, SERVICE_FEE_RATE, UPLOAD_DIR, DB_FILE,
} from './db.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const DIST = path.join(ROOT, 'dist');
const PORT = Number(process.env.PORT || 3003);
const IS_PROD = process.env.NODE_ENV === 'production';

// ── tiny .env loader (no dependency) ─────────────────────────────────────────
(function loadEnv() {
  const file = path.join(ROOT, '.env');
  if (!fs.existsSync(file)) return;
  for (const line of fs.readFileSync(file, 'utf8').split('\n')) {
    const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/i);
    if (!m) continue;
    const key = m[1];
    let val = m[2].trim();
    if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) val = val.slice(1, -1);
    if (!process.env[key]) process.env[key] = val;
  }
})();

const AI_PROVIDER = (process.env.AI_PROVIDER || 'zai').toLowerCase();
const AI_MODEL = process.env.AI_MODEL || 'glm-4.5';
const PROVIDERS = {
  zai: { base: process.env.ZAI_BASE_URL || 'https://api.z.ai/api/paas/v4', key: process.env.ZAI_API_KEY || '', model: AI_MODEL },
  openrouter: { base: process.env.OPENROUTER_BASE_URL || 'https://openrouter.ai/api/v1', key: process.env.OPENROUTER_API_KEY || '', model: process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini' },
  cohere: { base: process.env.COHERE_BASE_URL || 'https://api.cohere.ai/compatibility/v1', key: process.env.COHERE_API_KEY || '', model: process.env.COHERE_MODEL || 'command-a-03-2025' },
};
const PROVIDER_ORDER = [AI_PROVIDER, ...Object.keys(PROVIDERS).filter((p) => p !== AI_PROVIDER)];

// ── helpers ──────────────────────────────────────────────────────────────────
const nowIso = () => new Date().toISOString();
const money = (cents) => Math.round(cents);
const today = () => new Date().toISOString().slice(0, 10);

function send(res, status, data, headers = {}) {
  const body = JSON.stringify(data);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    ...headers,
  });
  res.end(body);
}

function sendErr(res, status, message, extra = {}) {
  send(res, status, { error: message, ...extra });
}

async function readBody(req, limit = 10 * 1024 * 1024) {
  if (req.body && typeof req.body === 'object' && Object.keys(req.body).length > 0) {
    return req.body;
  }
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on('data', (c) => {
      size += c.length;
      if (size > limit) {
        reject(Object.assign(new Error('Payload too large'), { status: 413 }));
        req.destroy();
        return;
      }
      chunks.push(c);
    });
    req.on('end', () => {
      if (!chunks.length) return resolve({});
      try {
        resolve(JSON.parse(Buffer.concat(chunks).toString('utf8')));
      } catch {
        reject(Object.assign(new Error('Invalid JSON body'), { status: 400 }));
      }
    });
    req.on('error', reject);
  });
}

function publicUser(u) {
  if (!u) return null;
  const { passwordHash, ...rest } = u;
  return rest;
}

function findUser(userId) {
  return db.users.find((u) => u.id === userId);
}

function findFreelancer(userId) {
  return db.freelancers.find((f) => f.userId === userId);
}

function findHirer(userId) {
  return db.hirers.find((h) => h.userId === userId);
}

function authUser(req) {
  const header = req.headers.authorization || '';
  const raw = header.startsWith('Bearer ') ? header.slice(7) : '';
  if (!raw) return null;
  const session = db.sessions.find((s) => s.token === raw);
  if (!session) return null;
  return findUser(session.userId) || null;
}

function requireAuth(req, res) {
  const user = authUser(req);
  if (!user) {
    sendErr(res, 401, 'Please sign in to continue.');
    return null;
  }
  return user;
}

function walletSummary(userId) {
  const user = findUser(userId);
  const held = db.bookings
    .filter((b) => (b.hirerId === userId || b.freelancerId === userId) && b.escrowCents > 0 &&
      ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'disputed'].includes(b.status))
    .reduce((sum, b) => sum + (b.freelancerId === userId ? Math.round(b.rateCents * b.days) : b.escrowCents), 0);
  const escrowPending = db.bookings
    .filter((b) => b.freelancerId === userId && b.escrowCents > 0 && ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'disputed'].includes(b.status))
    .reduce((sum, b) => sum + Math.round(b.rateCents * b.days), 0);
  const escrowHeld = db.bookings
    .filter((b) => b.hirerId === userId && b.escrowCents > 0 && ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'disputed'].includes(b.status))
    .reduce((sum, b) => sum + b.escrowCents, 0);
  return {
    balanceCents: Math.max(0, Math.round(user?.walletCents || 0)),
    escrowCents: held,
    escrowPendingCents: escrowPending,
    escrowHeldCents: escrowHeld,
    totalEarnedCents: db.transactions.filter((t) => t.userId === userId && t.type === 'escrow_release').reduce((s, t) => s + t.amountCents, 0),
  };
}

function notify(userId, type, title, body, extra = {}) {
  const n = { id: id('ntf'), userId, type, title, body, read: false, createdAt: nowIso(), ...extra };
  db.notifications.unshift(n);
  persist();
  return n;
}

function tx(userId, type, amountCents, description, extra = {}) {
  const t = { id: id('txn'), userId, type, amountCents, status: 'complete', description, createdAt: nowIso(), ...extra };
  db.transactions.unshift(t);
  persist();
  return t;
}

function visitorOrigin(user, query) {
  const city = query.get('city') || findHirer(user?.id)?.city || user?.city || 'Johannesburg';
  return { city, coords: cityCoords(city) };
}

function freelancerCard(f, origin, viewerId) {
  const user = findUser(f.userId);
  if (!user) return null;
  const distance = haversineKm(origin.coords, [f.lat, f.lng]);
  const visibility = user.id === viewerId ? 'self' : 'public';
  return {
    userId: f.userId,
    name: user.name,
    initials: user.initials || initialsOf(user.name),
    profession: f.profession,
    specialty: f.specialty,
    city: f.city,
    distanceKm: distance,
    dailyRateCents: f.dailyRateCents,
    yearsExperience: f.yearsExperience,
    rating: f.rating,
    reviewsCount: f.reviewsCount,
    completedGigs: f.completedGigs,
    verified: f.verified && (f.certifications || []).some((c) => c.verified),
    available: (f.availableDates || []).includes(today()) || (f.availableDates || []).some((d) => d > today() && d <= addDays(today(), 7)),
    skills: f.skills || [],
    certifications: (f.certifications || []).map((c) => ({ name: c.name, issuer: c.issuer, year: c.year, verified: c.verified })),
    education: f.education || [],
    bio: f.bio,
    visibility,
    phone: visibility === 'self' ? user.phone : undefined,
  };
}

function addDays(dateIso, days) {
  const d = new Date(dateIso);
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

function initialsOf(name = '') {
  return name.replace(/^(Dr\.?|Prof\.?)\s+/i, '').split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();
}

function bookingView(b, viewerId) {
  const hirer = findUser(b.hirerId);
  const pro = findUser(b.freelancerId);
  const fl = findFreelancer(b.freelancerId);
  const isParticipant = viewerId === b.hirerId || viewerId === b.freelancerId;
  const maySeeAddress = isParticipant && ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'released', 'closed', 'disputed'].includes(b.status);
  const other = viewerId === b.hirerId ? pro : hirer;
  const counterpart = other
    ? {
        id: other.id,
        name: other.name,
        initials: other.initials || initialsOf(other.name),
        profession: viewerId === b.hirerId ? fl?.profession : findHirer(b.hirerId)?.practiceName || 'Hirer',
        specialty: fl?.specialty,
        rating: fl?.rating,
        reviewsCount: fl?.reviewsCount,
        verified: fl?.verified,
        phone: maySeeAddress ? other.phone : undefined,
      }
    : null;
  return {
    ...b,
    address: maySeeAddress ? b.location : `${b.city} · exact address after acceptance`,
    counterpart,
    canReview: isParticipant,
  };
}

// ── AI provider ──────────────────────────────────────────────────────────────
let aiLastOk = null;
async function callLLM(messages, maxTokens = 420) {
  for (const name of PROVIDER_ORDER) {
    const p = PROVIDERS[name];
    if (!p?.key) continue;
    try {
      const res = await fetch(`${p.base.replace(/\/$/, '')}/chat/completions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${p.key}` },
        body: JSON.stringify({ model: p.model, messages, temperature: 0.45, max_tokens: maxTokens }),
        signal: AbortSignal.timeout(16000),
      });
      if (!res.ok) {
        console.warn(`[ai] ${name} ${res.status}: ${(await res.text()).slice(0, 160)}`);
        continue;
      }
      const data = await res.json();
      const text = data?.choices?.[0]?.message?.content?.trim();
      if (text) {
        aiLastOk = { provider: name, model: p.model, at: nowIso() };
        return { text, provider: name, model: p.model };
      }
    } catch (err) {
      console.warn(`[ai] ${name} failed: ${err?.message || err}`);
    }
  }
  return null;
}

function detectIntent(message = '') {
  const m = message.toLowerCase();
  if (/escrow|release|protect|refund|hold/.test(m)) return 'escrow';
  if (/earning|wallet|paid|payout|withdraw|balance|income/.test(m)) return 'earnings';
  if (/brief|shift|post|job description|cover/.test(m)) return 'brief';
  if (/find|search|match|need|looking for|hire|available|nurse|doctor|physio|radiographer|dentist|specialist|icu|paediatric|cardio/.test(m)) return 'find';
  return 'general';
}

function matchFreelancers(message, viewer) {
  const m = message.toLowerCase();
  const origin = visitorOrigin(viewer, new URLSearchParams());
  const words = m.split(/[^a-z]+/).filter((w) => w.length > 2);
  const results = db.freelancers
    .map((f) => ({ f, card: freelancerCard(f, origin, viewer?.id) }))
    .filter(({ card }) => !!card)
    .map(({ f, card }) => {
      const hay = `${card.name} ${card.profession} ${card.specialty} ${card.city} ${(card.skills || []).join(' ')}`.toLowerCase();
      let score = 0;
      for (const w of words) if (hay.includes(w)) score += w.length > 5 ? 3 : 1;
      if ((f.availableDates || []).includes(today())) score += 2;
      if (card.verified) score += 1;
      score += Math.min(card.rating, 5) * 0.4;
      return { ...card, score, availableToday: (f.availableDates || []).includes(today()) };
    })
    .sort((a, b) => b.score - a.score)
    .slice(0, 4);
  return results;
}

function bookingSummary(user) {
  const mine = db.bookings.filter((b) => b.hirerId === user.id || b.freelancerId === user.id);
  const active = mine.filter((b) => ['requested', 'pending_response', 'accepted', 'in_progress', 'completed_by_pro', 'pending_release'].includes(b.status));
  const wallet = walletSummary(user.id);
  return { total: mine.length, active: active.length, wallet, upcoming: active.slice(0, 4).map((b) => ({ ref: b.ref, role: b.roleTitle, dates: b.dateStart === b.dateEnd ? b.dateStart : `${b.dateStart} → ${b.dateEnd}`, status: b.status })) };
}

const COPILOT_SYSTEM = `You are MediGig Copilot, the in-app assistant for a South African medical freelance marketplace.
You help hirers (clinics, hospitals, practices) find locum medical professionals and help freelancers find shifts and understand their wallet.
Rules:
- Be concise: 2-5 short sentences, warm and professional. South African English. Use ZAR (R).
- Never invent people, rates, dates or addresses. Only use the MATCHES and CONTEXT provided.
- Escrow: hirer funds are held by MediGig when a booking is confirmed, and released to the professional 5 business days after the hirer confirms completion.
- Addresses are private until a booking is accepted; browsing shows distance in km only.
- Never give clinical advice. This is a staffing product.
- If MATCHES are provided, mention the best one by name, rate and distance.
- Do not use markdown headings or bullet lists longer than 3 items.`;

function aiFallback(intent, message, matches, user) {
  const role = findHirer(user.id) ? 'hire' : findFreelancer(user.id) ? 'work' : 'both';
  if (intent === 'find' && matches.length) {
    const top = matches[0];
    return `I found ${matches.length} professional${matches.length > 1 ? 's' : ''} that fit. ${top.name} is ${top.distanceKm} km away — ${top.profession}${top.specialty ? ` · ${top.specialty}` : ''} at R${(top.dailyRateCents / 100).toLocaleString('en-ZA')}/day${top.verified ? ', certification verified' : ''}. Tap a card to view the full profile or start a booking.`;
  }
  if (intent === 'escrow') return 'MediGig holds the hirer\'s payment in escrow the moment a booking is confirmed. After the hirer confirms the job is complete, the professional is paid out 5 business days later. If a shift is declined or cancelled early, the funds return to the hirer.';
  if (intent === 'earnings' || intent === 'wallet') {
    const w = walletSummary(user.id);
    return `Your wallet balance is R${(w.balanceCents / 100).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}${w.escrowPendingCents ? `, with R${(w.escrowPendingCents / 100).toLocaleString('en-ZA', { minimumFractionDigits: 2 })} still in escrow` : ''}. You can withdraw available funds to your bank account from the Wallet tab; escrow clears 5 business days after a hirer confirms completion.`;
  }
  if (intent === 'brief') return 'Tell me the profession, date and shift type and I\'ll draft a short shift brief you can paste into a post. For example: “ICU night shift, 2 nights, enrolled nurse, ventilator experience.”';
  if (role === 'hire') return 'I can match you with verified locums by profession, distance and day rate, draft a shift brief, or explain how escrow protects a booking. What do you need covered?';
  return 'I can find shifts near you, explain escrow and payouts, or summarise your MediGig wallet. What would you like to do?';
}

// ── API router ───────────────────────────────────────────────────────────────
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
  const pathname = decodeURIComponent(url.pathname);
  const method = req.method || 'GET';

  // CORS (dev)
  res.setHeader('Access-Control-Allow-Origin', req.headers.origin || '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  if (method === 'OPTIONS') {
    res.writeHead(204);
    return res.end();
  }

  try {
    // static uploads
    if (pathname.startsWith('/uploads/')) {
      const name = path.basename(pathname);
      const file = path.join(UPLOAD_DIR, name);
      if (!file.startsWith(UPLOAD_DIR) || !fs.existsSync(file)) return sendErr(res, 404, 'File not found');
      const ext = path.extname(file).toLowerCase();
      const mime = { '.pdf': 'application/pdf', '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.webp': 'image/webp' }[ext] || 'application/octet-stream';
      res.writeHead(200, { 'Content-Type': mime, 'Cache-Control': 'public, max-age=86400' });
      return fs.createReadStream(file).pipe(res);
    }

    if (pathname === '/api/health') {
      return send(res, 200, {
        ok: true, service: 'medigig-za', version: '1.0.0', db: 'json', uptime: Math.round(process.uptime()),
        ai: { provider: AI_PROVIDER, model: AI_MODEL, configured: PROVIDER_ORDER.some((p) => PROVIDERS[p]?.key), lastOk: aiLastOk },
      });
    }

    if (pathname === '/api/demo/reset' && method === 'POST') {
      if (process.env.ALLOW_DEMO_RESET === 'false') return sendErr(res, 403, 'Demo reset disabled');
      resetDb();
      return send(res, 200, { ok: true, reset: true });
    }

    // ── auth ──────────────────────────────────────────────────────────────
    if (pathname === '/api/auth/register' && method === 'POST') {
      const b = await readBody(req);
      const name = String(b.name || '').trim();
      const email = String(b.email || '').trim().toLowerCase();
      const phone = String(b.phone || '').trim();
      const password = String(b.password || '');
      const role = ['freelancer', 'hirer', 'both'].includes(b.role) ? b.role : 'freelancer';
      const city = String(b.city || '').trim() || (role === 'hirer' ? 'Johannesburg' : 'Cape Town');
      if (!name || name.length < 2) return sendErr(res, 400, 'Please enter your full name.');
      if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) return sendErr(res, 400, 'Please enter a valid email address.');
      if (password.length < 8) return sendErr(res, 400, 'Password must be at least 8 characters.');
      if (phone && !/^\+?27?\s?\d[\d\s-]{7,}$/.test(phone)) return sendErr(res, 400, 'Please enter a valid South African mobile number.');
      if (db.users.some((u) => u.email === email)) return sendErr(res, 409, 'An account with that email already exists.');
      const user = {
        id: id('usr'), name, email, phone: phone || '', role, city, passwordHash: hashPassword(password),
        walletCents: 0, initials: initialsOf(name), verified: false, createdAt: nowIso(),
      };
      db.users.push(user);
      if (role === 'freelancer' || role === 'both') {
        db.freelancers.push({
          userId: user.id, profession: String(b.profession || 'General Practitioner'), specialty: String(b.specialty || 'General Practice'),
          yearsExperience: 0, dailyRateCents: 150000, maxDistanceKm: 40, availableDates: [], skills: [], education: [],
          certifications: [], bio: '', rating: 0, reviewsCount: 0, completedGigs: 0, verified: false,
          city, lat: cityCoords(city)[0] + 0.01, lng: cityCoords(city)[1] + 0.01,
        });
      }
      if (role === 'hirer' || role === 'both') {
        db.hirers.push({
          userId: user.id, practiceName: String(b.practiceName || `${name} Practice`), practiceType: String(b.practiceType || 'Private Practice'),
          registrationNumber: String(b.registrationNumber || ''), contactPerson: name, city, address: city, staffCount: 0,
        });
      }
      const t = token();
      db.sessions.push({ token: t, userId: user.id, createdAt: nowIso() });
      persist();
      return send(res, 201, { token: t, user: publicUser(user) });
    }

    if (pathname === '/api/auth/login' && method === 'POST') {
      const b = await readBody(req);
      const email = String(b.email || '').trim().toLowerCase();
      const user = db.users.find((u) => u.email === email);
      if (!user || !verifyPassword(String(b.password || ''), user.passwordHash)) {
        return sendErr(res, 401, 'Email or password is incorrect.');
      }
      const t = token();
      db.sessions.push({ token: t, userId: user.id, createdAt: nowIso() });
      persist();
      return send(res, 200, { token: t, user: publicUser(user) });
    }

    if (pathname === '/api/auth/logout' && method === 'POST') {
      const header = req.headers.authorization || '';
      const raw = header.startsWith('Bearer ') ? header.slice(7) : '';
      db.sessions = db.sessions.filter((s) => s.token !== raw);
      persist();
      return send(res, 200, { ok: true });
    }

    // ── me / profiles ─────────────────────────────────────────────────────
    if (pathname === '/api/me' && method === 'GET') {
      const user = authUser(req);
      if (!user) return send(res, 200, { user: null });
      return send(res, 200, {
        user: publicUser(user),
        freelancer: findFreelancer(user.id) || null,
        hirer: findHirer(user.id) || null,
        wallet: walletSummary(user.id),
        paymentMethods: db.paymentMethods[user.id] || [],
        unread: db.notifications.filter((n) => n.userId === user.id && !n.read).length,
      });
    }

    if (pathname === '/api/profile' && method === 'PUT') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      if (b.name) user.name = String(b.name).trim();
      if (b.phone) user.phone = String(b.phone).trim();
      if (b.city) user.city = String(b.city).trim();
      if (b.role && ['freelancer', 'hirer', 'both'].includes(b.role) && user.role !== b.role) user.role = b.role;
      if (b.initials) user.initials = String(b.initials).slice(0, 3).toUpperCase();
      persist();
      return send(res, 200, { user: publicUser(user) });
    }

    if (pathname === '/api/profile/freelancer' && method === 'PUT') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      let f = findFreelancer(user.id);
      if (!f) {
        f = {
          userId: user.id, profession: 'General Practitioner', specialty: 'General Practice', yearsExperience: 0,
          dailyRateCents: 150000, maxDistanceKm: 40, availableDates: [], skills: [], education: [], certifications: [],
          bio: '', rating: 0, reviewsCount: 0, completedGigs: 0, verified: false, city: user.city,
          lat: cityCoords(user.city)[0], lng: cityCoords(user.city)[1],
        };
        db.freelancers.push(f);
      }
      if (b.profession !== undefined) f.profession = String(b.profession).slice(0, 80);
      if (b.specialty !== undefined) f.specialty = String(b.specialty).slice(0, 80);
      if (b.yearsExperience !== undefined) f.yearsExperience = Math.max(0, Math.min(50, Number(b.yearsExperience) || 0));
      if (b.dailyRateCents !== undefined) f.dailyRateCents = Math.max(20000, Math.min(2000000, Math.round(Number(b.dailyRateCents) || 0)));
      if (b.maxDistanceKm !== undefined) f.maxDistanceKm = Math.max(1, Math.min(300, Number(b.maxDistanceKm) || 40));
      if (Array.isArray(b.skills)) f.skills = b.skills.map((s) => String(s).slice(0, 40)).slice(0, 12);
      if (Array.isArray(b.education)) f.education = b.education.slice(0, 8);
      if (Array.isArray(b.certifications)) f.certifications = b.certifications.slice(0, 12);
      if (Array.isArray(b.availableDates)) f.availableDates = [...new Set(b.availableDates.filter((d) => /^\d{4}-\d{2}-\d{2}$/.test(d)))].sort();
      if (b.bio !== undefined) f.bio = String(b.bio).slice(0, 1200);
      if (b.city) { f.city = String(b.city); f.lat = cityCoords(f.city)[0] + 0.008; f.lng = cityCoords(f.city)[1] + 0.008; }
      persist();
      return send(res, 200, { freelancer: f });
    }

    if (pathname === '/api/profile/hirer' && method === 'PUT') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      let h = findHirer(user.id);
      if (!h) {
        h = { userId: user.id, practiceName: `${user.name} Practice`, practiceType: 'Private Practice', registrationNumber: '', contactPerson: user.name, city: user.city, address: user.city, staffCount: 0 };
        db.hirers.push(h);
      }
      for (const key of ['practiceName', 'practiceType', 'registrationNumber', 'contactPerson', 'city', 'address']) {
        if (b[key] !== undefined) h[key] = String(b[key]).slice(0, 240);
      }
      if (b.staffCount !== undefined) h.staffCount = Math.max(0, Number(b.staffCount) || 0);
      persist();
      return send(res, 200, { hirer: h });
    }

    // ── discovery ─────────────────────────────────────────────────────────
    if (pathname === '/api/freelancers' && method === 'GET') {
      const viewer = authUser(req);
      const origin = visitorOrigin(viewer, url.searchParams);
      const profession = (url.searchParams.get('profession') || '').toLowerCase();
      const specialty = (url.searchParams.get('specialty') || '').toLowerCase();
      const q = (url.searchParams.get('q') || '').toLowerCase();
      const maxRate = Number(url.searchParams.get('maxRate') || 0);
      const maxDistance = Number(url.searchParams.get('maxDistance') || 0);
      const date = url.searchParams.get('date') || '';
      const certified = url.searchParams.get('certified') === '1';
      const limit = Math.min(50, Number(url.searchParams.get('limit') || 24));

      let results = db.freelancers
        .map((f) => freelancerCard(f, origin, viewer?.id))
        .filter(Boolean)
        .filter((c) => !profession || c.profession.toLowerCase().includes(profession) || c.specialty.toLowerCase().includes(profession))
        .filter((c) => !specialty || c.specialty.toLowerCase().includes(specialty))
        .filter((c) => !maxRate || c.dailyRateCents <= maxRate * 100)
        .filter((c) => !maxDistance || c.distanceKm <= maxDistance)
        .filter((c) => !certified || c.verified)
        .filter((c) => !date || (db.freelancers.find((f) => f.userId === c.userId)?.availableDates || []).includes(date))
        .filter((c) => !q || `${c.name} ${c.profession} ${c.specialty} ${c.city} ${(c.skills || []).join(' ')}`.toLowerCase().includes(q));

      results.sort((a, b) => a.distanceKm - b.distanceKm || b.rating - a.rating);
      const professions = [...new Set(db.freelancers.map((f) => f.profession))].sort();
      return send(res, 200, { results: results.slice(0, limit), total: results.length, origin: origin.city, professions });
    }

    const proMatch = pathname.match(/^\/api\/freelancers\/([^/]+)$/);
    if (proMatch && method === 'GET') {
      const viewer = authUser(req);
      const origin = visitorOrigin(viewer, url.searchParams);
      const f = db.freelancers.find((x) => x.userId === proMatch[1]);
      if (!f) return sendErr(res, 404, 'Professional not found');
      const card = freelancerCard(f, origin, viewer?.id);
      return send(res, 200, { professional: card });
    }

    // ── bookings ──────────────────────────────────────────────────────────
    if (pathname === '/api/bookings' && method === 'GET') {
      const user = requireAuth(req, res);
      if (!user) return;
      const mine = db.bookings
        .filter((b) => b.hirerId === user.id || b.freelancerId === user.id)
        .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
        .map((b) => bookingView(b, user.id));
      return send(res, 200, { bookings: mine });
    }

    if (pathname === '/api/bookings' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      if (!['hirer', 'both'].includes(user.role) && !findHirer(user.id)) return sendErr(res, 403, 'Switch to hiring mode to book a professional.');
      const b = await readBody(req);
      const f = db.freelancers.find((x) => x.userId === b.freelancerId);
      const pro = findUser(b.freelancerId);
      if (!f || !pro) return sendErr(res, 404, 'Professional not found.');
      const dateStart = String(b.dateStart || '');
      const dateEnd = String(b.dateEnd || dateStart);
      if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStart) || !/^\d{4}-\d{2}-\d{2}$/.test(dateEnd)) return sendErr(res, 400, 'Choose a valid start and end date.');
      if (dateStart < today()) return sendErr(res, 400, 'Start date cannot be in the past.');
      if (dateEnd < dateStart) return sendErr(res, 400, 'End date must be after the start date.');
      const days = Math.max(1, Math.round((new Date(dateEnd) - new Date(dateStart)) / 86400000) + 1);
      if (days > 30) return sendErr(res, 400, 'Bookings are limited to 30 days.');
      const missing = [];
      for (let i = 0; i < days; i++) {
        const d = addDays(dateStart, i);
        if (!(f.availableDates || []).includes(d)) missing.push(d);
      }
      if (missing.length) return sendErr(res, 409, `${pro.name} is not available on ${missing.slice(0, 3).join(', ')}.`, { unavailableDates: missing });
      const rate = f.dailyRateCents;
      const subtotal = rate * days;
      const fee = Math.round(subtotal * SERVICE_FEE_RATE);
      const total = subtotal + fee;
      if (user.walletCents < total) {
        return sendErr(res, 402, `You need R${((total - user.walletCents) / 100).toFixed(2)} more in your wallet. Top up to hold the escrow.`, { shortfallCents: total - user.walletCents, totalCents: total });
      }
      const hirer = findHirer(user.id);
      user.walletCents -= total;
      const booking = {
        id: id('bkg'), ref: `MG-${1000 + db.bookings.length + 1}`, hirerId: user.id, freelancerId: b.freelancerId,
        roleTitle: `${f.profession} · ${b.shiftType || f.specialty || 'Locum shift'}`,
        dateStart, dateEnd, timeSlot: String(b.timeSlot || '08:00–17:00'), days,
        rateCents: rate, serviceFeeCents: fee, totalCents: total, escrowCents: total,
        location: hirer?.address || `${user.city} · address shared on acceptance`, city: hirer?.city || user.city,
        distanceKm: haversineKm(cityCoords(hirer?.city || user.city), [f.lat, f.lng]),
        shiftType: String(b.shiftType || f.specialty || 'Locum'), notes: String(b.notes || '').slice(0, 800),
        status: 'pending_response', createdAt: nowIso(),
        timeline: [{ at: nowIso(), label: `Booking requested by ${user.name} · R${(total / 100).toFixed(2)} held in escrow`, by: 'hirer' }],
      };
      db.bookings.unshift(booking);
      tx(user.id, 'escrow_hold', -total, `Escrow · ${booking.ref} ${booking.roleTitle}`, { bookingId: booking.id, status: 'held', ref: booking.ref });
      notify(b.freelancerId, 'request', 'New job request', `${hirer?.practiceName || user.name} wants to hire you for ${booking.roleTitle} · ${dateStart}${dateEnd !== dateStart ? ` → ${dateEnd}` : ''} · ${booking.distanceKm} km away.`, { bookingId: booking.id, actions: ['accept', 'decline'] });
      notify(user.id, 'escrow', 'Funds held in escrow', `R${(total / 100).toFixed(2)} is held for ${pro.name} until the job is completed and confirmed.`, { bookingId: booking.id });
      persist();
      return send(res, 201, { booking: bookingView(booking, user.id), wallet: walletSummary(user.id) });
    }

    const bookingAction = pathname.match(/^\/api\/bookings\/([^/]+)\/([a-z-]+)$/);
    if (bookingAction && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const booking = db.bookings.find((b) => b.id === bookingAction[1] || b.ref === bookingAction[1]);
      if (!booking) return sendErr(res, 404, 'Booking not found.');
      const isHirer = booking.hirerId === user.id;
      const isPro = booking.freelancerId === user.id;
      if (!isHirer && !isPro) return sendErr(res, 403, 'You are not part of this booking.');
      const action = bookingAction[2];
      const body = await readBody(req).catch(() => ({}));
      const stamp = (label, by) => booking.timeline.push({ at: nowIso(), label, by });

      const refund = () => {
        if (booking.escrowCents > 0) {
          const hirer = findUser(booking.hirerId);
          if (hirer) hirer.walletCents += booking.escrowCents;
          tx(booking.hirerId, 'escrow_refund', booking.escrowCents, `Escrow returned · ${booking.ref}`, { bookingId: booking.id, ref: booking.ref });
          booking.escrowCents = 0;
        }
      };

      if (action === 'accept') {
        if (!isPro) return sendErr(res, 403, 'Only the professional can accept.');
        if (!['pending_response', 'requested'].includes(booking.status)) return sendErr(res, 409, 'This booking can no longer be accepted.');
        booking.status = 'accepted';
        booking.acceptedAt = nowIso();
        stamp('Accepted — full address released', 'freelancer');
        notify(booking.hirerId, 'accepted', 'Booking accepted', `${user.name} accepted ${booking.roleTitle} (${booking.ref}). Contact details and the full address are now visible.`, { bookingId: booking.id });
        notify(booking.freelancerId, 'address', 'Address unlocked', `${booking.location}`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'decline') {
        if (!isPro) return sendErr(res, 403, 'Only the professional can decline.');
        if (!['pending_response', 'requested'].includes(booking.status)) return sendErr(res, 409, 'This booking can no longer be declined.');
        booking.status = 'declined';
        refund();
        stamp('Declined by professional — escrow returned to hirer', 'freelancer');
        notify(booking.hirerId, 'declined', 'Booking declined', `${user.name} declined ${booking.roleTitle}. The escrow has been returned to your wallet.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'hirer-accept') {
        if (!isHirer) return sendErr(res, 403, 'Only the hirer can approve this request.');
        if (booking.status !== 'requested') return sendErr(res, 409, 'This request is no longer open.');
        if (user.walletCents < booking.totalCents) return sendErr(res, 402, 'Top up your wallet to fund escrow for this shift.', { shortfallCents: booking.totalCents - user.walletCents });
        user.walletCents -= booking.totalCents;
        booking.status = 'accepted';
        booking.escrowCents = booking.totalCents;
        booking.acceptedAt = nowIso();
        tx(user.id, 'escrow_hold', -booking.totalCents, `Escrow · ${booking.ref} ${booking.roleTitle}`, { bookingId: booking.id, status: 'held', ref: booking.ref });
        stamp(`Approved by hirer — R${(booking.totalCents / 100).toFixed(2)} held in escrow`, 'hirer');
        notify(booking.freelancerId, 'accepted', 'Shift approved', `Your request for ${booking.roleTitle} was approved. Full address: ${booking.location}`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id), wallet: walletSummary(user.id) });
      }

      if (action === 'hirer-decline') {
        if (!isHirer) return sendErr(res, 403, 'Only the hirer can decline this request.');
        if (booking.status !== 'requested') return sendErr(res, 409, 'This request is no longer open.');
        booking.status = 'declined';
        stamp('Declined by hirer', 'hirer');
        notify(booking.freelancerId, 'declined', 'Shift request declined', `Your request for ${booking.roleTitle} on ${booking.dateStart} was declined.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'start') {
        if (booking.status !== 'accepted') return sendErr(res, 409, 'Only accepted bookings can start.');
        booking.status = 'in_progress';
        stamp('Shift started', user.id === booking.freelancerId ? 'freelancer' : 'hirer');
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'complete') {
        if (!isPro) return sendErr(res, 403, 'Only the professional can mark a shift complete.');
        if (!['accepted', 'in_progress'].includes(booking.status)) return sendErr(res, 409, 'This booking cannot be completed.');
        booking.status = 'completed_by_pro';
        booking.completedAt = nowIso();
        stamp('Marked complete by professional', 'freelancer');
        notify(booking.hirerId, 'completion', 'Job completed — confirm to release', `${user.name} marked ${booking.roleTitle} complete. R${(booking.escrowCents / 100).toFixed(2)} is in escrow; confirm completion to start the release timer.`, { bookingId: booking.id, actions: ['confirm'] });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'confirm') {
        if (!isHirer) return sendErr(res, 403, 'Only the hirer can confirm completion.');
        if (!['completed_by_pro', 'in_progress', 'accepted'].includes(booking.status)) return sendErr(res, 409, 'This booking cannot be confirmed yet.');
        booking.status = 'pending_release';
        booking.completedAt = booking.completedAt || nowIso();
        booking.releaseAt = addBusinessDays(nowIso(), 5);
        stamp(`Completion confirmed — release scheduled for ${new Date(booking.releaseAt).toLocaleDateString('en-ZA')}`, 'hirer');
        notify(booking.freelancerId, 'escrow', 'Completion confirmed', `${findUser(booking.hirerId)?.name} confirmed ${booking.roleTitle}. R${(booking.rateCents * booking.days / 100).toFixed(2)} releases to your wallet on ${new Date(booking.releaseAt).toLocaleDateString('en-ZA')}.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'release') {
        if (booking.status !== 'pending_release') return sendErr(res, 409, 'Nothing to release yet.');
        const pro = findUser(booking.freelancerId);
        const amount = Math.round(booking.rateCents * booking.days);
        if (pro) pro.walletCents += amount;
        tx(booking.freelancerId, 'escrow_release', amount, `Escrow released · ${booking.ref} ${booking.roleTitle}`, { bookingId: booking.id, ref: booking.ref });
        booking.status = 'released';
        booking.releasedAt = nowIso();
        booking.escrowCents = 0;
        stamp(`R${(amount / 100).toFixed(2)} released to professional`, 'system');
        notify(booking.freelancerId, 'payout', 'Escrow released', `R${(amount / 100).toFixed(2)} landed in your MediGig wallet from ${booking.ref}.`, { bookingId: booking.id });
        notify(booking.hirerId, 'payout', 'Escrow released', `The escrow for ${booking.ref} was released to ${pro?.name}.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'cancel') {
        if (!['pending_response', 'requested', 'accepted'].includes(booking.status)) return sendErr(res, 409, 'This booking can no longer be cancelled.');
        booking.status = 'cancelled';
        refund();
        stamp('Booking cancelled — escrow returned', user.id === booking.hirerId ? 'hirer' : 'freelancer');
        notify(booking.hirerId === user.id ? booking.freelancerId : booking.hirerId, 'cancelled', 'Booking cancelled', `${booking.roleTitle} (${booking.ref}) was cancelled.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'rate') {
        if (!isHirer) return sendErr(res, 403, 'Only the hirer can rate this job.');
        if (!['completed_by_pro', 'pending_release', 'released'].includes(booking.status)) return sendErr(res, 409, 'Rate the job once it is complete.');
        const stars = Math.max(1, Math.min(5, Number(body.stars) || 5));
        booking.rating = stars;
        booking.review = String(body.review || '').slice(0, 600);
        booking.status = 'closed';
        const f = findFreelancer(booking.freelancerId);
        if (f) {
          f.rating = Math.round((((f.rating || 0) * (f.reviewsCount || 0) + stars) / ((f.reviewsCount || 0) + 1)) * 10) / 10;
          f.reviewsCount = (f.reviewsCount || 0) + 1;
          f.completedGigs = (f.completedGigs || 0) + 1;
        }
        stamp(`Rated ${stars}★ by hirer`, 'hirer');
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      if (action === 'dispute') {
        if (booking.status !== 'pending_release') return sendErr(res, 409, 'Disputes are open while escrow is pending release.');
        booking.status = 'disputed';
        stamp('Dispute opened — release paused', 'hirer');
        notify(booking.freelancerId, 'dispute', 'A dispute was opened', `The hirer opened a dispute on ${booking.ref}. MediGig support will review within 48 hours.`, { bookingId: booking.id });
        persist();
        return send(res, 200, { booking: bookingView(booking, user.id) });
      }

      return sendErr(res, 400, 'Unknown booking action.');
    }

    // ── wallet ────────────────────────────────────────────────────────────
    if (pathname === '/api/wallet' && method === 'GET') {
      const user = requireAuth(req, res);
      if (!user) return;
      const txs = db.transactions.filter((t) => t.userId === user.id).slice(0, 100);
      const earningsByMonth = {};
      for (const t of txs) {
        if (t.type !== 'escrow_release') continue;
        const key = t.createdAt.slice(0, 7);
        earningsByMonth[key] = (earningsByMonth[key] || 0) + t.amountCents;
      }
      return send(res, 200, {
        wallet: walletSummary(user.id),
        transactions: txs,
        paymentMethods: db.paymentMethods[user.id] || [],
        earningsByMonth,
        feeRate: SERVICE_FEE_RATE,
      });
    }

    if (pathname === '/api/wallet/topup' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const amountCents = Math.round(Number(b.amountCents || 0));
      if (amountCents < 10000) return sendErr(res, 400, 'Minimum top-up is R100.');
      if (amountCents > 5000000) return sendErr(res, 400, 'Maximum top-up is R50,000.');
      const method = ['payfast', 'paystack', 'card', 'eft'].includes(b.method) ? b.method : 'payfast';
      user.walletCents += amountCents;
      const ref = `${method.toUpperCase()}-${crypto.randomBytes(3).toString('hex').toUpperCase()}`;
      tx(user.id, 'topup', amountCents, `Wallet top-up · ${method === 'payfast' ? 'PayFast' : method === 'paystack' ? 'Paystack' : method === 'eft' ? 'Instant EFT' : 'Card'}`, { ref });
      notify(user.id, 'wallet', 'Wallet topped up', `R${(amountCents / 100).toFixed(2)} added via ${method}. Reference ${ref}.`, {});
      persist();
      return send(res, 200, { wallet: walletSummary(user.id), reference: ref });
    }

    if (pathname === '/api/wallet/withdraw' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const amountCents = Math.round(Number(b.amountCents || 0));
      if (amountCents < 10000) return sendErr(res, 400, 'Minimum withdrawal is R100.');
      if (amountCents > user.walletCents) return sendErr(res, 402, 'Withdrawal is more than your available balance.');
      user.walletCents -= amountCents;
      const bank = String(b.bank || 'FNB •••• 7781');
      tx(user.id, 'withdrawal', -amountCents, `Withdrawal to ${bank}`, { status: 'processing' });
      notify(user.id, 'payout', 'Withdrawal requested', `R${(amountCents / 100).toFixed(2)} is on its way to ${bank}. 1–2 business days.`, {});
      persist();
      return send(res, 200, { wallet: walletSummary(user.id), bank });
    }

    if (pathname === '/api/wallet/payment-methods' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const list = db.paymentMethods[user.id] || [];
      list.push({ id: id('pm'), type: b.type === 'bank' ? 'bank' : 'card', label: String(b.label || 'New method'), primary: list.length === 0 });
      db.paymentMethods[user.id] = list;
      persist();
      return send(res, 200, { paymentMethods: list });
    }

    // ── notifications ─────────────────────────────────────────────────────
    if (pathname === '/api/notifications' && method === 'GET') {
      const user = requireAuth(req, res);
      if (!user) return;
      const list = db.notifications.filter((n) => n.userId === user.id).slice(0, 50);
      return send(res, 200, { notifications: list, unread: list.filter((n) => !n.read).length });
    }

    if (pathname === '/api/notifications/read' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      for (const n of db.notifications) {
        if (n.userId !== user.id) continue;
        if (b.all || (Array.isArray(b.ids) && b.ids.includes(n.id))) n.read = true;
      }
      persist();
      return send(res, 200, { ok: true });
    }

    // ── uploads (base64 JSON, no multipart dependency) ────────────────────
    if (pathname === '/api/uploads' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req, 8 * 1024 * 1024);
      const mime = String(b.mime || 'application/pdf');
      const allowed = { 'application/pdf': '.pdf', 'image/png': '.png', 'image/jpeg': '.jpg', 'image/webp': '.webp' };
      if (!allowed[mime]) return sendErr(res, 400, 'Only PDF, PNG, JPG or WEBP files are accepted.');
      const b64 = String(b.data || '').replace(/^data:[^,]+,/, '');
      const buf = Buffer.from(b64, 'base64');
      if (!buf.length || buf.length > 6 * 1024 * 1024) return sendErr(res, 400, 'File must be smaller than 6 MB.');
      const name = `cert_${user.id}_${Date.now()}${allowed[mime]}`;
      fs.writeFileSync(path.join(UPLOAD_DIR, name), buf);
      persist();
      return send(res, 200, { url: `/uploads/${name}`, name: String(b.name || name).slice(0, 160), size: buf.length });
    }

    // ── posts (open shifts) ───────────────────────────────────────────────
    if (pathname === '/api/posts' && method === 'GET') {
      const viewer = authUser(req);
      const list = db.posts.map((p) => {
        const h = findHirer(p.hirerId);
        return { ...p, practiceName: h?.practiceName, practiceType: h?.practiceType, city: p.city || h?.city };
      });
      return send(res, 200, { posts: list.filter((p) => p.status === 'open'), viewer: viewer?.id || null });
    }

    if (pathname === '/api/posts' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const post = {
        id: id('pst'), hirerId: user.id, title: String(b.title || 'Locum shift').slice(0, 120),
        profession: String(b.profession || 'Any').slice(0, 60), specialty: String(b.specialty || '').slice(0, 60),
        city: String(b.city || user.city), dateStart: String(b.dateStart || today()), dateEnd: String(b.dateEnd || b.dateStart || today()),
        timeSlot: String(b.timeSlot || '08:00–17:00'), rateCents: Math.max(20000, Math.round(Number(b.rateCents) || 150000)),
        notes: String(b.notes || '').slice(0, 800), status: 'open', createdAt: nowIso(),
      };
      db.posts.unshift(post);
      persist();
      return send(res, 201, { post });
    }

    const postRequest = pathname.match(/^\/api\/posts\/([^/]+)\/request$/);
    if (postRequest && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const f = findFreelancer(user.id);
      if (!f) return sendErr(res, 403, 'Add a professional profile before requesting shifts.');
      const post = db.posts.find((p) => p.id === postRequest[1]);
      if (!post || post.status !== 'open') return sendErr(res, 404, 'This shift is no longer open.');
      if (db.bookings.some((b) => b.postId === post.id && b.freelancerId === user.id && !['declined', 'cancelled'].includes(b.status))) {
        return sendErr(res, 409, 'You have already requested this shift.');
      }
      const hirer = findHirer(post.hirerId);
      const days = Math.max(1, Math.round((new Date(post.dateEnd) - new Date(post.dateStart)) / 86400000) + 1);
      const subtotal = post.rateCents * days;
      const fee = Math.round(subtotal * SERVICE_FEE_RATE);
      const booking = {
        id: id('bkg'), ref: `MG-${1000 + db.bookings.length + 1}`, postId: post.id,
        hirerId: post.hirerId, freelancerId: user.id, roleTitle: `${f.profession} · ${post.specialty || post.title}`,
        dateStart: post.dateStart, dateEnd: post.dateEnd, timeSlot: post.timeSlot, days,
        rateCents: post.rateCents, serviceFeeCents: fee, totalCents: subtotal + fee, escrowCents: 0,
        location: hirer?.address || post.city, city: post.city,
        distanceKm: haversineKm(cityCoords(post.city), [f.lat, f.lng]),
        shiftType: post.specialty || 'Locum', notes: post.notes || '', status: 'requested', createdAt: nowIso(),
        timeline: [{ at: nowIso(), label: `${user.name} requested this shift`, by: 'freelancer' }],
      };
      db.bookings.unshift(booking);
      notify(post.hirerId, 'request', 'New shift request', `${user.name} (${f.profession}) requested ${post.title} · ${post.dateStart}.`, { bookingId: booking.id, actions: ['hirer-accept', 'hirer-decline'] });
      notify(user.id, 'request', 'Request sent', `Your request for ${post.title} was sent to ${hirer?.practiceName || 'the practice'}. You'll be notified once approved.`, { bookingId: booking.id });
      persist();
      return send(res, 201, { booking: bookingView(booking, user.id) });
    }

    // ── AI copilot ────────────────────────────────────────────────────────
    if (pathname === '/api/ai/status' && method === 'GET') {
      return send(res, 200, {
        configured: PROVIDER_ORDER.some((p) => PROVIDERS[p]?.key),
        provider: AI_PROVIDER, model: AI_MODEL, lastOk: aiLastOk,
        features: ['matching', 'shift-brief', 'escrow-help', 'earnings-summary'],
      });
    }

    if (pathname === '/api/ai/copilot' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const message = String(b.message || '').slice(0, 1200).trim();
      if (!message) return sendErr(res, 400, 'Tell the copilot what you need.');
      const intent = detectIntent(message);
      const matches = intent === 'find' ? matchFreelancers(message, user) : [];
      const wallet = walletSummary(user.id);
      const bookings = bookingSummary(user);
      const role = findHirer(user.id) ? (findFreelancer(user.id) ? 'both' : 'hirer') : 'freelancer';
      const context = {
        user: { name: user.name, role, city: user.city },
        wallet: { available: wallet.balanceCents / 100, escrowPending: wallet.escrowPendingCents / 100 },
        bookings: bookings.upcoming,
        matches: matches.map((m) => ({ name: m.name, profession: m.profession, specialty: m.specialty, ratePerDay: m.dailyRateCents / 100, km: m.distanceKm, verified: m.verified, availableToday: m.availableToday })),
      };
      const started = Date.now();
      const llm = await callLLM([
        { role: 'system', content: COPILOT_SYSTEM },
        { role: 'user', content: `CONTEXT:\n${JSON.stringify(context)}\n\nUSER MESSAGE:\n${message}` },
      ], 420);
      const reply = llm?.text || aiFallback(intent, message, matches, user);
      const suggestions = role === 'hirer' || role === 'both'
        ? ['Find an ICU nurse near me', 'Draft a shift brief for a Saturday clinic', 'How does escrow protect me?']
        : ['Find cardiology shifts in Johannesburg', 'When do I get paid?', 'Summarise my earnings'];
      persist();
      return send(res, 200, {
        reply,
        intent,
        matches: matches.map((m) => ({
          userId: m.userId, name: m.name, initials: m.initials, profession: m.profession, specialty: m.specialty,
          dailyRateCents: m.dailyRateCents, distanceKm: m.distanceKm, verified: m.verified, rating: m.rating, reviewsCount: m.reviewsCount, city: m.city,
        })),
        suggestions,
        provider: llm?.provider || 'medigig-rules',
        model: llm?.model || 'fallback',
        latencyMs: Date.now() - started,
      });
    }

    if (pathname === '/api/ai/brief' && method === 'POST') {
      const user = requireAuth(req, res);
      if (!user) return;
      const b = await readBody(req);
      const brief = {
        title: String(b.title || 'Locum shift'),
        profession: String(b.profession || 'Medical professional'),
        dateStart: String(b.dateStart || today()),
        dateEnd: String(b.dateEnd || b.dateStart || today()),
        timeSlot: String(b.timeSlot || '08:00–17:00'),
        city: String(b.city || user.city),
        rateCents: Math.round(Number(b.rateCents) || 150000),
        notes: String(b.notes || ''),
      };
      const fallback = `We need a ${brief.profession} for ${brief.title} in ${brief.city} on ${brief.dateStart}${brief.dateEnd !== brief.dateStart ? ` to ${brief.dateEnd}` : ''} (${brief.timeSlot}). Day rate R${(brief.rateCents / 100).toLocaleString('en-ZA')}. ${brief.notes || 'Must be HPCSA/SANC registered with current certification.'} Escrow-protected booking via MediGig ZA.`;
      const llm = await callLLM([
        { role: 'system', content: 'You write short, clear shift briefs for a South African medical staffing marketplace. 3-5 sentences, no markdown, include dates, times, rate in ZAR, registration requirement, and that payment is escrow-protected.' },
        { role: 'user', content: JSON.stringify(brief) },
      ], 260);
      return send(res, 200, { brief: llm?.text || fallback, provider: llm?.provider || 'medigig-rules', model: llm?.model || 'fallback' });
    }

    // ── stats (landing + dashboards) ──────────────────────────────────────
    if (pathname === '/api/stats' && method === 'GET') {
      const released = db.transactions.filter((t) => t.type === 'escrow_release');
      return send(res, 200, {
        professionals: db.freelancers.length,
        practices: db.hirers.length,
        gigs: db.bookings.filter((b) => b.status === 'closed').length + 128,
        paidOutCents: released.reduce((s, t) => s + t.amountCents, 0) + 4820000,
        avgFillHours: 6,
        cities: [...new Set(db.freelancers.map((f) => f.city))].length,
      });
    }

    // ── production static ─────────────────────────────────────────────────
    if (!pathname.startsWith('/api')) {
      if (IS_PROD && fs.existsSync(DIST)) {
        const rel = pathname === '/' ? 'index.html' : pathname.replace(/^\/+/, '');
        let file = path.join(DIST, rel);
        if (!file.startsWith(DIST) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) file = path.join(DIST, 'index.html');
        const ext = path.extname(file).toLowerCase();
        const mime = {
          '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8',
          '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.webp': 'image/webp',
          '.woff2': 'font/woff2', '.json': 'application/json', '.ico': 'image/x-icon',
        }[ext] || 'application/octet-stream';
        res.writeHead(200, { 'Content-Type': mime, 'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=31536000, immutable' });
        return fs.createReadStream(file).pipe(res);
      }
      if (!IS_PROD) return sendErr(res, 404, 'Dev UI is served by Vite on :5173');
    }

    return sendErr(res, 404, 'Not found');
  } catch (err) {
    console.error('[api]', err);
    return sendErr(res, err?.status || 500, err?.message || 'Something went wrong.');
  }
});

if (process.env.AUTO_LISTEN === 'true') {
  server.listen(PORT, () => {
    console.log(`MediGig ZA API on http://localhost:${PORT} · ${IS_PROD ? 'production (static + api)' : 'development api'} · db ${DB_FILE}`);
    console.log(`AI provider: ${AI_PROVIDER}/${AI_MODEL} · configured=${PROVIDER_ORDER.some((p) => PROVIDERS[p]?.key)}`);
  });
}

export default server;

