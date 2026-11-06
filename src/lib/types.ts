export type Role = 'freelancer' | 'hirer' | 'both';
export type Mode = 'work' | 'hire';

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: Role;
  city: string;
  walletCents: number;
  initials?: string;
  verified?: boolean;
  createdAt: string;
}

export interface Education {
  qualification: string;
  institution: string;
  year?: number | string;
}

export interface Certification {
  name: string;
  number?: string;
  issuer?: string;
  year?: number | string;
  verified?: boolean;
  url?: string;
  fileName?: string;
}

export interface Freelancer {
  userId: string;
  profession: string;
  specialty: string;
  yearsExperience: number;
  dailyRateCents: number;
  maxDistanceKm: number;
  availableDates: string[];
  skills: string[];
  education: Education[];
  certifications: Certification[];
  bio: string;
  rating: number;
  reviewsCount: number;
  completedGigs: number;
  verified: boolean;
  city: string;
}

export interface Hirer {
  userId: string;
  practiceName: string;
  practiceType: string;
  registrationNumber: string;
  contactPerson: string;
  city: string;
  address: string;
  staffCount: number;
}

export interface Wallet {
  balanceCents: number;
  escrowCents: number;
  escrowPendingCents: number;
  escrowHeldCents: number;
  totalEarnedCents: number;
}

export interface PaymentMethod {
  id: string;
  type: string;
  label: string;
  expiry?: string;
  primary?: boolean;
}

export interface MeResponse {
  user: User | null;
  freelancer: Freelancer | null;
  hirer: Hirer | null;
  wallet: Wallet | null;
  paymentMethods: PaymentMethod[];
  unread: number;
}

export type BookingStatus =
  | 'requested'
  | 'pending_response'
  | 'accepted'
  | 'in_progress'
  | 'completed_by_pro'
  | 'pending_release'
  | 'released'
  | 'closed'
  | 'declined'
  | 'cancelled'
  | 'disputed';

export interface Counterpart {
  id: string;
  name: string;
  initials: string;
  profession?: string;
  specialty?: string;
  rating?: number;
  reviewsCount?: number;
  verified?: boolean;
  phone?: string;
}

export interface Booking {
  id: string;
  ref: string;
  hirerId: string;
  freelancerId: string;
  roleTitle: string;
  dateStart: string;
  dateEnd: string;
  timeSlot: string;
  days: number;
  rateCents: number;
  serviceFeeCents: number;
  totalCents: number;
  escrowCents: number;
  location: string;
  city: string;
  distanceKm: number;
  shiftType: string;
  notes: string;
  status: BookingStatus;
  createdAt: string;
  acceptedAt?: string;
  completedAt?: string;
  releaseAt?: string;
  releasedAt?: string;
  rating?: number;
  review?: string;
  counterpart: Counterpart | null;
  timeline: { at: string; label: string; by: string }[];
  canReview?: boolean;
}

export interface ProfessionalCard {
  userId: string;
  name: string;
  initials: string;
  profession: string;
  specialty: string;
  city: string;
  distanceKm: number;
  dailyRateCents: number;
  yearsExperience: number;
  rating: number;
  reviewsCount: number;
  completedGigs: number;
  verified: boolean;
  available: boolean;
  skills: string[];
  certifications: Certification[];
  education: Education[];
  bio: string;
  visibility?: string;
  phone?: string;
}

export interface Transaction {
  id: string;
  userId: string;
  type: string;
  amountCents: number;
  status: string;
  description: string;
  ref?: string;
  bookingId?: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  type: string;
  title: string;
  body: string;
  read: boolean;
  bookingId?: string;
  actions?: string[];
  createdAt: string;
}

export interface Post {
  id: string;
  hirerId: string;
  title: string;
  profession: string;
  specialty: string;
  city: string;
  dateStart: string;
  dateEnd: string;
  timeSlot: string;
  rateCents: number;
  notes: string;
  status: string;
  practiceName?: string;
  practiceType?: string;
  createdAt: string;
}

export interface AiMatch {
  userId: string;
  name: string;
  initials: string;
  profession: string;
  specialty: string;
  dailyRateCents: number;
  distanceKm: number;
  verified: boolean;
  rating: number;
  reviewsCount: number;
  city: string;
}

export interface AiReply {
  reply: string;
  intent: string;
  matches: AiMatch[];
  suggestions: string[];
  provider: string;
  model: string;
  latencyMs: number;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  meta?: { provider?: string; model?: string; latencyMs?: number };
  matches?: AiMatch[];
  thinking?: string[];
  pending?: boolean;
}
