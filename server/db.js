import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const DATA_DIR = path.join(__dirname, 'data');
export const DB_FILE = path.join(DATA_DIR, 'db.json');
export const UPLOAD_DIR = path.join(__dirname, 'uploads');

export const SERVICE_FEE_RATE = 0.05;

const CITIES = {
  'cape town': [-33.9249, 18.4241],
  'johannesburg': [-26.2041, 28.0473],
  'sandton': [-26.1076, 28.0567],
  'soweto': [-26.2485, 27.854],
  'pretoria': [-25.7479, 28.2293],
  'durban': [-29.8587, 31.0218],
  'port elizabeth': [-33.9608, 25.6022],
  'gqeberha': [-33.9608, 25.6022],
  'bloemfontein': [-29.0852, 26.1596],
  'stellenbosch': [-33.9321, 18.8602],
  'polokwane': [-23.9045, 29.4689],
  'mbombela': [-25.4753, 30.9694],
  'nelspruit': [-25.4753, 30.9694],
  'east london': [-33.0292, 27.8546],
  'kimberley': [-28.7282, 24.7499],
  'pietermaritzburg': [-29.6006, 30.3794],
};

export function cityCoords(city) {
  const key = String(city || '').trim().toLowerCase();
  if (CITIES[key]) return CITIES[key];
  for (const [name, coords] of Object.entries(CITIES)) {
    if (key.includes(name) || name.includes(key)) return coords;
  }
  return CITIES['johannesburg'];
}

export function haversineKm(a, b) {
  const R = 6371;
  const toRad = (v) => (v * Math.PI) / 180;
  const dLat = toRad(b[0] - a[0]);
  const dLon = toRad(b[1] - a[1]);
  const lat1 = toRad(a[0]);
  const lat2 = toRad(b[0]);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2;
  return Math.round(2 * R * Math.asin(Math.sqrt(h)) * 10) / 10;
}

export function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(String(password), salt, 64).toString('hex');
  return `${salt}:${hash}`;
}

export function verifyPassword(password, stored) {
  try {
    const [salt, hash] = String(stored || '').split(':');
    if (!salt || !hash) return false;
    const candidate = crypto.scryptSync(String(password), salt, 64);
    const expected = Buffer.from(hash, 'hex');
    return candidate.length === expected.length && crypto.timingSafeEqual(candidate, expected);
  } catch {
    return false;
  }
}

export const id = (prefix) => `${prefix}_${crypto.randomBytes(6).toString('hex')}`;
export const token = () => crypto.randomBytes(24).toString('hex');

export function isoNow() {
  return new Date().toISOString();
}

export function dayOffset(n) {
  const d = new Date();
  d.setHours(12, 0, 0, 0);
  d.setDate(d.getDate() + n);
  return d.toISOString().slice(0, 10);
}

export function addBusinessDays(dateIso, days) {
  const d = new Date(dateIso);
  let added = 0;
  while (added < days) {
    d.setDate(d.getDate() + 1);
    const dow = d.getDay();
    if (dow !== 0 && dow !== 6) added += 1;
  }
  return d.toISOString();
}

function seedAvailability() {
  const dates = [];
  for (let i = 1; i <= 45; i++) {
    const d = new Date();
    d.setHours(12, 0, 0, 0);
    d.setDate(d.getDate() + i);
    if (d.getDay() === 0) continue;
    if (i % 7 === 3) continue; // a few blocked days
    dates.push(d.toISOString().slice(0, 10));
  }
  return dates;
}

function demoUsers() {
  const now = isoNow();
  return [
    {
      id: 'usr_netcare', name: 'Dr. Naledi Khumalo', email: 'netcare@medigig.za', phone: '+27 82 555 0142',
      role: 'hirer', passwordHash: hashPassword('demo1234'), city: 'Johannesburg', createdAt: now,
      walletCents: 530000, initials: 'NK', verified: true,
    },
    {
      id: 'usr_mokoena', name: 'Dr. Lerato Mokoena', email: 'doctor@medigig.za', phone: '+27 83 555 0198',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Johannesburg', createdAt: now,
      walletCents: 1250000, initials: 'LM', verified: true,
    },
    {
      id: 'usr_both', name: 'Dr. Sibusiso Ndlovu', email: 'both@medigig.za', phone: '+27 84 555 0121',
      role: 'both', passwordHash: hashPassword('demo1234'), city: 'Sandton', createdAt: now,
      walletCents: 800000, initials: 'SN', verified: false,
    },
    {
      id: 'usr_thandi', name: 'Thandi Mabaso', email: 'thandi@medigig.za', phone: '+27 81 555 0111',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Johannesburg', createdAt: now,
      walletCents: 640000, initials: 'TM', verified: true,
    },
    {
      id: 'usr_sipho', name: 'Sipho Khumalo', email: 'sipho@medigig.za', phone: '+27 82 555 0155',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Soweto', createdAt: now,
      walletCents: 218000, initials: 'SK', verified: true,
    },
    {
      id: 'usr_leratop', name: 'Lerato Pillay', email: 'leratop@medigig.za', phone: '+27 83 555 0134',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Sandton', createdAt: now,
      walletCents: 415000, initials: 'LP', verified: true,
    },
    {
      id: 'usr_johnson', name: 'Dr. Michael Johnson', email: 'm.johnson@medigig.za', phone: '+27 82 555 0177',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Cape Town', createdAt: now,
      walletCents: 960000, initials: 'MJ', verified: true,
    },
    {
      id: 'usr_kagiso', name: 'Kagiso Ndlovu', email: 'kagiso@medigig.za', phone: '+27 84 555 0166',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Cape Town', createdAt: now,
      walletCents: 305000, initials: 'KN', verified: false,
    },
    {
      id: 'usr_naidoo', name: 'Dr. Anisha Naidoo', email: 'a.naidoo@medigig.za', phone: '+27 81 555 0188',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Durban', createdAt: now,
      walletCents: 720000, initials: 'AN', verified: true,
    },
    {
      id: 'usr_dlamini', name: 'Dr. Zanele Dlamini', email: 'z.dlamini@medigig.za', phone: '+27 83 555 0199',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Pretoria', createdAt: now,
      walletCents: 540000, initials: 'ZD', verified: true,
    },
    {
      id: 'usr_palesa', name: 'Palesa Mahlangu', email: 'palesa@medigig.za', phone: '+27 82 555 0163',
      role: 'freelancer', passwordHash: hashPassword('demo1234'), city: 'Bloemfontein', createdAt: now,
      walletCents: 190000, initials: 'PM', verified: true,
    },
  ];
}

function freelancer(userId, data) {
  return {
    userId,
    bio: '',
    skills: [],
    education: [],
    certifications: [],
    rating: 5,
    reviewsCount: 0,
    completedGigs: 0,
    verified: false,
    maxDistanceKm: 60,
    availableDates: seedAvailability(),
    ...data,
  };
}

export function seedDb() {
  const users = demoUsers();
  const availability = seedAvailability();
  const freelancers = [
    freelancer('usr_mokoena', {
      profession: 'Specialist', specialty: 'General Cardiology', yearsExperience: 12,
      dailyRateCents: 180000, rating: 5.0, reviewsCount: 12, completedGigs: 34, verified: true,
      city: 'Johannesburg', lat: -26.175, lng: 28.032, maxDistanceKm: 45,
      bio: 'HPCSA-registered cardiologist available for locum consulting, ward rounds and echo reporting across Gauteng. Calm under pressure, fastidious notes.',
      skills: ['Echocardiography', 'ECG interpretation', 'Ward rounds', 'Cardiac rehab'],
      education: [
        { qualification: 'MBChB', institution: 'University of the Witwatersrand', year: 2011 },
        { qualification: 'MMed (Cardiology)', institution: 'University of Pretoria', year: 2018 },
      ],
      certifications: [
        { name: 'HPCSA Registration', number: 'MP 0451237', issuer: 'HPCSA', year: 2012, verified: true },
        { name: 'Advanced Cardiac Life Support', number: 'ACLS-2025-881', issuer: 'Resuscitation Council of SA', year: 2025, verified: true },
      ],
      availableDates: availability,
    }),
    freelancer('usr_thandi', {
      profession: 'Registered Nurse', specialty: 'General Ward', yearsExperience: 6,
      dailyRateCents: 95000, rating: 5.0, reviewsCount: 12, completedGigs: 41, verified: true,
      city: 'Johannesburg', lat: -26.191, lng: 28.041, maxDistanceKm: 35,
      bio: 'Registered nurse with strong general ward and post-op experience. Known for empathetic patient care and tidy handovers.',
      skills: ['General ward', 'Post-operative care', 'Wound care', 'IV therapy'],
      education: [{ qualification: 'Diploma in Nursing', institution: 'Netcare Education', year: 2018 }],
      certifications: [{ name: 'SANC Registration', number: 'RN 118842', issuer: 'SANC', year: 2019, verified: true }],
    }),
    freelancer('usr_sipho', {
      profession: 'Enrolled Nurse', specialty: 'ICU / Night Shift', yearsExperience: 4,
      dailyRateCents: 72000, rating: 4.8, reviewsCount: 8, completedGigs: 22, verified: true,
      city: 'Soweto', lat: -26.2485, lng: 27.854, maxDistanceKm: 50,
      bio: 'Enrolled nurse comfortable with high-acuity night shifts, ventilator checks and ICU documentation.',
      skills: ['ICU', 'Night shift', 'Ventilator checks', 'Observation charts'],
      education: [{ qualification: 'Certificate in Nursing', institution: 'Chris Hani Baragwanath Nursing College', year: 2020 }],
      certifications: [{ name: 'SANC Registration', number: 'EN 204991', issuer: 'SANC', year: 2021, verified: true }],
    }),
    freelancer('usr_leratop', {
      profession: 'Physiotherapist', specialty: 'Rehabilitation', yearsExperience: 8,
      dailyRateCents: 110000, rating: 4.9, reviewsCount: 15, completedGigs: 29, verified: true,
      city: 'Sandton', lat: -26.1076, lng: 28.0567, maxDistanceKm: 40,
      bio: 'Physiotherapist specialising in post-orthopaedic rehab and in-hospital mobility programmes.',
      skills: ['Orthopaedic rehab', 'Neuro rehab', 'Chest physio', 'Mobility assessment'],
      education: [{ qualification: 'BSc Physiotherapy', institution: 'University of the Witwatersrand', year: 2016 }],
      certifications: [{ name: 'HPCSA Registration', number: 'PT 009812', issuer: 'HPCSA', year: 2017, verified: true }],
    }),
    freelancer('usr_johnson', {
      profession: 'General Practitioner', specialty: 'General Practice', yearsExperience: 9,
      dailyRateCents: 200000, rating: 4.9, reviewsCount: 34, completedGigs: 58, verified: true,
      city: 'Cape Town', lat: -33.941, lng: 18.441, maxDistanceKm: 30,
      bio: 'Independent GP for locum cover in Cape Town. Comfortable with acute consults, chronic scripts and minor procedures.',
      skills: ['Acute consults', 'Chronic care', 'Minor procedures', 'Family medicine'],
      education: [{ qualification: 'MBChB', institution: 'University of Cape Town', year: 2015 }],
      certifications: [{ name: 'HPCSA Registration', number: 'MP 0382211', issuer: 'HPCSA', year: 2016, verified: true }],
    }),
    freelancer('usr_kagiso', {
      profession: 'Registered Nurse', specialty: 'Theatre / Recovery', yearsExperience: 5,
      dailyRateCents: 145000, rating: 4.7, reviewsCount: 6, completedGigs: 18, verified: false,
      city: 'Cape Town', lat: -33.91, lng: 18.399, maxDistanceKm: 25,
      bio: 'Theatre and recovery nurse, available for day-case lists and post-anaesthetic recovery cover.',
      skills: ['Theatre', 'Recovery', 'Scrub nurse', 'PACU'],
      education: [{ qualification: 'Diploma in Nursing', institution: 'Stellenbosch University', year: 2019 }],
      certifications: [{ name: 'SANC Registration', number: 'RN 132004', issuer: 'SANC', year: 2020, verified: false }],
    }),
    freelancer('usr_naidoo', {
      profession: 'Specialist', specialty: 'Paediatrics', yearsExperience: 14,
      dailyRateCents: 240000, rating: 4.8, reviewsCount: 21, completedGigs: 40, verified: true,
      city: 'Durban', lat: -29.851, lng: 31.03, maxDistanceKm: 35,
      bio: 'Paediatrician covering outpatient clinics, neonatal rounds and after-hours call in KwaZulu-Natal.',
      skills: ['Neonatology', 'Paediatric clinics', 'After-hours call', 'Developmental assessment'],
      education: [{ qualification: 'MBChB', institution: 'University of KwaZulu-Natal', year: 2010 }, { qualification: 'FC Paed', institution: 'CMSA', year: 2016 }],
      certifications: [{ name: 'HPCSA Registration', number: 'MP 0312290', issuer: 'HPCSA', year: 2011, verified: true }],
    }),
    freelancer('usr_dlamini', {
      profession: 'Dentist', specialty: 'General Dentistry', yearsExperience: 7,
      dailyRateCents: 220000, rating: 4.7, reviewsCount: 11, completedGigs: 19, verified: true,
      city: 'Pretoria', lat: -25.757, lng: 28.238, maxDistanceKm: 40,
      bio: 'General dentist available for locum days, restorative lists and emergency pain relief.',
      skills: ['Restorative', 'Extractions', 'Emergency dentistry', 'Paediatric dentistry'],
      education: [{ qualification: 'BChD', institution: 'University of Pretoria', year: 2017 }],
      certifications: [{ name: 'HPCSA Registration', number: 'DN 007734', issuer: 'HPCSA', year: 2018, verified: true }],
    }),
    freelancer('usr_palesa', {
      profession: 'Radiographer', specialty: 'Diagnostic Imaging', yearsExperience: 6,
      dailyRateCents: 150000, rating: 4.9, reviewsCount: 9, completedGigs: 26, verified: true,
      city: 'Bloemfontein', lat: -29.11, lng: 26.19, maxDistanceKm: 60,
      bio: 'Diagnostic radiographer for X-ray and CT lists. PACS-fluent and quick to slot into a busy department.',
      skills: ['X-ray', 'CT', 'PACS', 'Trauma imaging'],
      education: [{ qualification: 'BSc Radiography', institution: 'University of the Free State', year: 2018 }],
      certifications: [{ name: 'HPCSA Registration', number: 'RA 005518', issuer: 'HPCSA', year: 2019, verified: true }],
    }),
    freelancer('usr_both', {
      profession: 'General Practitioner', specialty: 'Emergency Medicine', yearsExperience: 10,
      dailyRateCents: 195000, rating: 4.8, reviewsCount: 17, completedGigs: 31, verified: false,
      city: 'Sandton', lat: -26.1076, lng: 28.0567, maxDistanceKm: 45,
      bio: 'GP with emergency medicine experience. Also runs a small family practice in Midrand — happy to hire locums too.',
      skills: ['Emergency medicine', 'Trauma', 'Family medicine', 'Suturing'],
      education: [{ qualification: 'MBChB', institution: 'University of Limpopo', year: 2014 }],
      certifications: [{ name: 'HPCSA Registration', number: 'MP 0419920', issuer: 'HPCSA', year: 2015, verified: false }],
    }),
  ];

  const hirers = [
    {
      userId: 'usr_netcare', practiceName: 'Netcare Management', practiceType: 'Private Hospital',
      registrationNumber: 'MP 0123456', contactPerson: 'Dr. Naledi Khumalo', city: 'Johannesburg',
      address: 'Netcare Park Lane, 1 Junction Avenue, Parktown, Johannesburg, 2193', staffCount: 12,
    },
    {
      userId: 'usr_both', practiceName: 'Midrand Family Clinic', practiceType: 'Private Practice',
      registrationNumber: 'BHF 0045121', contactPerson: 'Dr. Sibusiso Ndlovu', city: 'Sandton',
      address: '18 Allandale Road, Midrand, 1685', staffCount: 6,
    },
  ];

  const bookings = [];
  const transactions = [];
  const notifications = [];

  const mkBooking = (b) => {
    const days = b.days ?? 1;
    const subtotal = b.rateCents * days;
    const fee = Math.round(subtotal * SERVICE_FEE_RATE);
    const booking = {
      id: id('bkg'), ref: `MG-${String(1000 + bookings.length + 1)}`, days,
      serviceFeeCents: fee, totalCents: subtotal + fee, status: 'requested',
      escrowCents: 0, createdAt: isoNow(), timeline: [], ...b,
    };
    bookings.push(booking);
    return booking;
  };

  // Netcare: three freelancer shift requests waiting on the hirer
  mkBooking({
    hirerId: 'usr_netcare', freelancerId: 'usr_thandi', roleTitle: 'Registered Nurse · General Ward',
    dateStart: dayOffset(1), dateEnd: dayOffset(1), timeSlot: '09:00–17:00', rateCents: 95000,
    location: 'Netcare Park Lane · Parktown', city: 'Johannesburg', distanceKm: 4.2, shiftType: 'General Ward',
    notes: 'General ward support, post-op handovers.',
    timeline: [{ at: isoNow(), label: 'Thandi requested this shift', by: 'freelancer' }],
  });
  mkBooking({
    hirerId: 'usr_netcare', freelancerId: 'usr_sipho', roleTitle: 'Enrolled Nurse · ICU Night',
    dateStart: dayOffset(1), dateEnd: dayOffset(1), timeSlot: '19:00–07:00', rateCents: 72000,
    location: 'Netcare Park Lane · Parktown', city: 'Johannesburg', distanceKm: 13.4, shiftType: 'ICU / Night Shift',
    notes: 'ICU night cover, 1:2 acuity.',
    timeline: [{ at: isoNow(), label: 'Sipho requested this shift', by: 'freelancer' }],
  });
  mkBooking({
    hirerId: 'usr_netcare', freelancerId: 'usr_leratop', roleTitle: 'Physiotherapist · Rehab Unit',
    dateStart: dayOffset(2), dateEnd: dayOffset(2), timeSlot: '08:00–12:00', rateCents: 110000,
    location: 'Netcare Rehabilitation Hospital · Auckland Park', city: 'Johannesburg', distanceKm: 11.8, shiftType: 'Rehab Unit',
    notes: 'Post-orthopaedic rehab sessions.',
    timeline: [{ at: isoNow(), label: 'Lerato requested this shift', by: 'freelancer' }],
  });

  // Mokoena: completed job awaiting hirer confirmation (mockup screen 9)
  mkBooking({
    hirerId: 'usr_netcare', freelancerId: 'usr_mokoena', roleTitle: 'Cardiology Consultation',
    dateStart: dayOffset(-3), dateEnd: dayOffset(-2), timeSlot: '08:00–17:00', rateCents: 180000, days: 2,
    location: 'Netcare Park Lane · Parktown', city: 'Johannesburg', distanceKm: 6.5, shiftType: 'Cardiology',
    status: 'completed_by_pro', escrowCents: 360000, completedAt: new Date(Date.now() - 26 * 3600 * 1000).toISOString(),
    timeline: [
      { at: new Date(Date.now() - 6 * 86400000).toISOString(), label: 'Booking confirmed · funds in escrow', by: 'system' },
      { at: new Date(Date.now() - 3 * 86400000).toISOString(), label: 'Shift started', by: 'freelancer' },
      { at: new Date(Date.now() - 26 * 3600 * 1000).toISOString(), label: 'Job marked complete by Dr. Mokoena', by: 'freelancer' },
    ],
  });

  // Mokoena: upcoming accepted gig
  mkBooking({
    hirerId: 'usr_netcare', freelancerId: 'usr_mokoena', roleTitle: 'Cardiology Ward Round',
    dateStart: dayOffset(7), dateEnd: dayOffset(8), timeSlot: '08:00–17:00', rateCents: 180000, days: 2,
    location: 'Netcare Park Lane · Parktown', city: 'Johannesburg', distanceKm: 6.5, shiftType: 'Cardiology',
    status: 'accepted', escrowCents: 360000, acceptedAt: isoNow(),
    timeline: [
      { at: isoNow(), label: 'Booking confirmed · funds in escrow', by: 'system' },
      { at: isoNow(), label: 'Accepted by Dr. Lerato Mokoena', by: 'freelancer' },
    ],
  });

  // Released history for the earnings chart
  for (const [daysAgo, amount, ref] of [[34, 320000, 'MG-0944'], [67, 930000, 'MG-0881']]) {
    bookings.push({
      id: id('bkg'), ref, hirerId: 'usr_netcare', freelancerId: 'usr_mokoena',
      roleTitle: 'Cardiology Locum', dateStart: dayOffset(-daysAgo), dateEnd: dayOffset(-daysAgo + 1),
      timeSlot: '08:00–17:00', days: 2, rateCents: Math.round(amount / 2), serviceFeeCents: Math.round(amount * 0.05),
      totalCents: amount + Math.round(amount * 0.05), status: 'closed', escrowCents: 0,
      rating: 5, review: 'Excellent locum — clear notes and great with patients.', city: 'Johannesburg',
      location: 'Netcare Park Lane · Parktown', distanceKm: 6.5, shiftType: 'Cardiology',
      completedAt: new Date(Date.now() - daysAgo * 86400000).toISOString(),
      releasedAt: new Date(Date.now() - (daysAgo - 6) * 86400000).toISOString(),
      createdAt: new Date(Date.now() - (daysAgo + 8) * 86400000).toISOString(),
      timeline: [{ at: isoNow(), label: 'Funds released', by: 'system' }],
    });
  }

  // Wallet ledger
  transactions.push(
    { id: id('txn'), userId: 'usr_netcare', type: 'topup', amountCents: 1250000, status: 'complete', description: 'Wallet top-up · PayFast', ref: 'PAY-88213', createdAt: new Date(Date.now() - 12 * 86400000).toISOString() },
    { id: id('txn'), userId: 'usr_netcare', type: 'escrow_hold', amountCents: -360000, status: 'held', description: 'Escrow · MG-1004 Cardiology Consultation', ref: 'MG-1004', bookingId: bookings[3].id, createdAt: new Date(Date.now() - 6 * 86400000).toISOString() },
    { id: id('txn'), userId: 'usr_netcare', type: 'escrow_hold', amountCents: -360000, status: 'held', description: 'Escrow · MG-1005 Cardiology Ward Round', ref: 'MG-1005', bookingId: bookings[4].id, createdAt: isoNow() },
    { id: id('txn'), userId: 'usr_mokoena', type: 'escrow_release', amountCents: 320000, status: 'complete', description: 'Escrow released · MG-0944 Cardiology Locum', ref: 'MG-0944', createdAt: new Date(Date.now() - 28 * 86400000).toISOString() },
    { id: id('txn'), userId: 'usr_mokoena', type: 'escrow_release', amountCents: 930000, status: 'complete', description: 'Escrow released · MG-0881 Cardiology Locum', ref: 'MG-0881', createdAt: new Date(Date.now() - 61 * 86400000).toISOString() },
    { id: id('txn'), userId: 'usr_thandi', type: 'escrow_release', amountCents: 640000, status: 'complete', description: 'Escrow released · MG-0912 General Ward', ref: 'MG-0912', createdAt: new Date(Date.now() - 18 * 86400000).toISOString() },
  );

  notifications.push(
    { id: id('ntf'), userId: 'usr_netcare', type: 'request', title: '3 new shift requests', body: 'Thandi M., Sipho K. and Lerato P. want to cover your open shifts.', read: false, createdAt: isoNow() },
    { id: id('ntf'), userId: 'usr_netcare', type: 'completion', title: 'Job completed — confirm to release', body: 'Dr. Lerato Mokoena marked the Cardiology Consultation complete. R3,600 is in escrow.', bookingId: bookings[3].id, read: false, createdAt: new Date(Date.now() - 26 * 3600 * 1000).toISOString() },
    { id: id('ntf'), userId: 'usr_mokoena', type: 'accepted', title: 'Shift confirmed', body: 'Netcare Management confirmed your Cardiology Ward Round. Full address is now visible.', bookingId: bookings[4].id, read: false, createdAt: isoNow() },
    { id: id('ntf'), userId: 'usr_mokoena', type: 'payout', title: 'Escrow released', body: 'R3,200.00 landed in your MediGig wallet from MG-0944.', read: true, createdAt: new Date(Date.now() - 28 * 86400000).toISOString() },
  );

  const posts = [
    { id: id('pst'), hirerId: 'usr_netcare', title: 'ICU night shift · 2 nights', profession: 'Enrolled Nurse', specialty: 'ICU', city: 'Johannesburg', dateStart: dayOffset(1), dateEnd: dayOffset(2), timeSlot: '19:00–07:00', rateCents: 72000, notes: '1:2 acuity, ventilator experience required.', status: 'open', createdAt: isoNow() },
    { id: id('pst'), hirerId: 'usr_netcare', title: 'General ward day shift', profession: 'Registered Nurse', specialty: 'General Ward', city: 'Johannesburg', dateStart: dayOffset(1), dateEnd: dayOffset(1), timeSlot: '09:00–17:00', rateCents: 95000, notes: 'Post-op handovers and wound care.', status: 'open', createdAt: isoNow() },
  ];

  return {
    meta: { version: 1, createdAt: isoNow(), seeded: true },
    users, freelancers, hirers, bookings, transactions, notifications, posts,
    sessions: [],
    paymentMethods: {
      'usr_netcare': [{ id: 'pm_1', type: 'card', label: 'Visa •••• 4242', expiry: '09/28', primary: true }],
      'usr_mokoena': [{ id: 'pm_2', type: 'bank', label: 'FNB •••• 7781', primary: true }],
      'usr_both': [{ id: 'pm_3', type: 'card', label: 'Mastercard •••• 5518', expiry: '02/29', primary: true }],
    },
  };
}

fs.mkdirSync(DATA_DIR, { recursive: true });
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

let db;
if (fs.existsSync(DB_FILE)) {
  try {
    db = JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
  } catch {
    db = seedDb();
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
  }
} else {
  db = seedDb();
  fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
}

for (const key of ['users', 'freelancers', 'hirers', 'bookings', 'transactions', 'notifications', 'posts', 'sessions']) {
  if (!Array.isArray(db[key])) db[key] = [];
}
if (!db.paymentMethods) db.paymentMethods = {};

let writeTimer = null;
export function persist() {
  if (writeTimer) return;
  writeTimer = setTimeout(() => {
    writeTimer = null;
    const tmp = `${DB_FILE}.tmp`;
    fs.writeFileSync(tmp, JSON.stringify(db, null, 2));
    fs.renameSync(tmp, DB_FILE);
  }, 40);
}

export function resetDb() {
  db = seedDb();
  fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
  return db;
}

export default db;
export { db, CITIES };
