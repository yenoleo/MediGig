export function cn(...parts: Array<string | false | null | undefined>) {
  return parts.filter(Boolean).join(' ');
}

export function zar(cents: number, opts: { decimals?: boolean; sign?: boolean } = {}) {
  const value = (cents || 0) / 100;
  const formatted = value.toLocaleString('en-ZA', {
    minimumFractionDigits: opts.decimals ? 2 : 0,
    maximumFractionDigits: opts.decimals ? 2 : 0,
  });
  return `${opts.sign && value > 0 ? '+' : ''}R${formatted}`;
}

export function zarShort(cents: number) {
  const v = (cents || 0) / 100;
  if (Math.abs(v) >= 1_000_000) return `R${(v / 1_000_000).toFixed(1)}m`;
  if (Math.abs(v) >= 10_000) return `R${(v / 1000).toFixed(0)}k`;
  if (Math.abs(v) >= 1_000) return `R${(v / 1000).toFixed(1)}k`;
  return `R${v.toFixed(0)}`;
}

export function parseRand(value: string) {
  return Math.max(0, Math.round(Number(String(value).replace(/[^\d.]/g, '')) * 100));
}

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export function fmtDate(iso?: string, opts: { weekday?: boolean; year?: boolean } = {}) {
  if (!iso) return '—';
  const d = new Date(iso.length <= 10 ? `${iso}T12:00:00` : iso);
  if (Number.isNaN(d.getTime())) return iso;
  const day = d.getDate();
  const mon = MONTHS[d.getMonth()];
  const year = opts.year === false ? '' : ` ${d.getFullYear()}`;
  return `${opts.weekday ? `${DAYS[d.getDay()]} ` : ''}${day} ${mon}${year}`;
}

export function fmtRange(start?: string, end?: string) {
  if (!start) return '—';
  if (!end || end === start) return fmtDate(start);
  return `${fmtDate(start)} → ${fmtDate(end)}`;
}

export function fmtTime(iso?: string) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleTimeString('en-ZA', { hour: '2-digit', minute: '2-digit' });
}

export function relativeTime(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.round(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.round(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  if (days === 1) return 'yesterday';
  if (days < 30) return `${days}d ago`;
  return fmtDate(iso, { year: false });
}

export function initialsOf(name = '') {
  return name
    .replace(/^(Dr\.?|Prof\.?|Mr\.?|Mrs\.?|Ms\.?)\s+/i, '')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0])
    .join('')
    .toUpperCase();
}

export const todayIso = () => new Date().toISOString().slice(0, 10);

export function addDaysIso(iso: string, days: number) {
  const d = new Date(`${iso}T12:00:00`);
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

export function daysBetween(start: string, end: string) {
  if (!start || !end) return 1;
  return Math.max(1, Math.round((new Date(end).getTime() - new Date(start).getTime()) / 86400000) + 1);
}

export interface StatusMeta {
  label: string;
  short: string;
  tone: string;
  dot: string;
}

export const statusMeta: Record<string, StatusMeta> = {
  requested: { label: 'New shift request', short: 'Request', tone: 'bg-amber-50 text-amber-800 border-amber-200', dot: 'bg-amber-500' },
  pending_response: { label: 'Awaiting response', short: 'Pending', tone: 'bg-amber-50 text-amber-800 border-amber-200', dot: 'bg-amber-500' },
  accepted: { label: 'Confirmed · in escrow', short: 'Confirmed', tone: 'bg-teal-50 text-teal-800 border-teal-200', dot: 'bg-teal-500' },
  in_progress: { label: 'Shift in progress', short: 'In progress', tone: 'bg-sky-50 text-sky-800 border-sky-200', dot: 'bg-sky-500' },
  completed_by_pro: { label: 'Awaiting your confirmation', short: 'Completed', tone: 'bg-indigo-50 text-indigo-800 border-indigo-200', dot: 'bg-indigo-500' },
  pending_release: { label: 'Escrow pending release', short: 'Releasing', tone: 'bg-violet-50 text-violet-800 border-violet-200', dot: 'bg-violet-500' },
  released: { label: 'Paid out', short: 'Paid', tone: 'bg-emerald-50 text-emerald-800 border-emerald-200', dot: 'bg-emerald-500' },
  closed: { label: 'Closed', short: 'Closed', tone: 'bg-slate-100 text-slate-600 border-slate-200', dot: 'bg-slate-400' },
  declined: { label: 'Declined', short: 'Declined', tone: 'bg-rose-50 text-rose-700 border-rose-200', dot: 'bg-rose-400' },
  cancelled: { label: 'Cancelled', short: 'Cancelled', tone: 'bg-slate-100 text-slate-600 border-slate-200', dot: 'bg-slate-400' },
  disputed: { label: 'Disputed · release paused', short: 'Disputed', tone: 'bg-rose-50 text-rose-700 border-rose-200', dot: 'bg-rose-500' },
};

export function monthMatrix(year: number, month: number) {
  const first = new Date(year, month, 1);
  const start = new Date(first);
  start.setDate(1 - first.getDay());
  const cells: { date: Date; iso: string; current: boolean }[] = [];
  for (let i = 0; i < 42; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    cells.push({ date: d, iso: d.toISOString().slice(0, 10), current: d.getMonth() === month });
  }
  return cells;
}

export function greeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 17) return 'Good afternoon';
  return 'Good evening';
}

export function pct(n: number, d: number) {
  if (!d) return 0;
  return Math.max(0, Math.min(100, Math.round((n / d) * 100)));
}

export function plural(n: number, one: string, many = `${one}s`) {
  return `${n} ${n === 1 ? one : many}`;
}

export const PROFESSIONS = [
  'General Practitioner',
  'Specialist',
  'Registered Nurse',
  'Enrolled Nurse',
  'Physiotherapist',
  'Dentist',
  'Radiographer',
  'Pharmacist',
  'Psychologist',
  'Paramedic',
];

export const CITIES = [
  'Cape Town',
  'Johannesburg',
  'Sandton',
  'Soweto',
  'Pretoria',
  'Durban',
  'Port Elizabeth',
  'Bloemfontein',
  'Stellenbosch',
  'Polokwane',
  'Mbombela',
  'East London',
  'Kimberley',
  'Pietermaritzburg',
];

export const TIME_SLOTS = ['07:00–19:00', '08:00–17:00', '09:00–17:00', '08:00–13:00', '14:00–22:00', '19:00–07:00'];

export const SHIFT_TYPES = ['General Ward', 'ICU', 'Casualty', 'Theatre', 'Cardiology', 'Paediatrics', 'Rehab Unit', 'Outpatient', 'Night Shift'];
