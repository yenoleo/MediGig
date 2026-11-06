import type {
  AiReply, Booking, Freelancer, Hirer, MeResponse, Notification, Post, ProfessionalCard,
  Role, Transaction, User, Wallet,
} from './types';

const TOKEN_KEY = 'medigig_token';

export class ApiError extends Error {
  status: number;
  payload: any;
  constructor(message: string, status: number, payload?: any) {
    super(message);
    this.status = status;
    this.payload = payload;
  }
}

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t: string | null) => {
  if (t) localStorage.setItem(TOKEN_KEY, t);
  else localStorage.removeItem(TOKEN_KEY);
};

async function request<T>(path: string, options: { method?: string; body?: any; auth?: boolean } = {}): Promise<T> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (options.auth !== false && token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(path, {
    method: options.method || 'GET',
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
  let data: any = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }
  if (!res.ok) {
    throw new ApiError(data?.error || `Request failed (${res.status})`, res.status, data);
  }
  return data as T;
}

export const api = {
  health: () => request<any>('/api/health', { auth: false }),
  stats: () => request<{ professionals: number; practices: number; gigs: number; paidOutCents: number; avgFillHours: number; cities: number }>('/api/stats', { auth: false }),

  login: (email: string, password: string) =>
    request<{ token: string; user: User }>('/api/auth/login', { method: 'POST', body: { email, password } }),
  register: (body: Record<string, any>) =>
    request<{ token: string; user: User }>('/api/auth/register', { method: 'POST', body }),
  logout: () => request<{ ok: boolean }>('/api/auth/logout', { method: 'POST' }),
  me: () => request<MeResponse>('/api/me'),

  updateProfile: (body: Partial<User>) => request<{ user: User }>('/api/profile', { method: 'PUT', body }),
  updateFreelancer: (body: Partial<Freelancer>) => request<{ freelancer: Freelancer }>('/api/profile/freelancer', { method: 'PUT', body }),
  updateHirer: (body: Partial<Hirer>) => request<{ hirer: Hirer }>('/api/profile/hirer', { method: 'PUT', body }),

  freelancers: (params: Record<string, string | number | undefined>) => {
    const q = new URLSearchParams();
    for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== '' && v !== null) q.set(k, String(v));
    return request<{ results: ProfessionalCard[]; total: number; origin: string; professions: string[] }>(`/api/freelancers?${q.toString()}`);
  },
  professional: (id: string) => request<{ professional: ProfessionalCard }>(`/api/freelancers/${id}`),

  bookings: () => request<{ bookings: Booking[] }>('/api/bookings'),
  createBooking: (body: Record<string, any>) => request<{ booking: Booking; wallet: Wallet }>('/api/bookings', { method: 'POST', body }),
  bookingAction: (id: string, action: string, body: Record<string, any> = {}) =>
    request<{ booking: Booking; wallet?: Wallet }>(`/api/bookings/${id}/${action}`, { method: 'POST', body }),

  wallet: () => request<{ wallet: Wallet; transactions: Transaction[]; paymentMethods: any[]; earningsByMonth: Record<string, number>; feeRate: number }>('/api/wallet'),
  topUp: (amountCents: number, method: string) => request<{ wallet: Wallet; reference: string }>('/api/wallet/topup', { method: 'POST', body: { amountCents, method } }),
  withdraw: (amountCents: number, bank: string) => request<{ wallet: Wallet; bank: string }>('/api/wallet/withdraw', { method: 'POST', body: { amountCents, bank } }),
  addPaymentMethod: (body: Record<string, any>) => request<{ paymentMethods: any[] }>('/api/wallet/payment-methods', { method: 'POST', body }),

  notifications: () => request<{ notifications: Notification[]; unread: number }>('/api/notifications'),
  readNotifications: (body: { all?: boolean; ids?: string[] }) => request<{ ok: boolean }>('/api/notifications/read', { method: 'POST', body }),

  upload: (file: { name: string; mime: string; data: string }) => request<{ url: string; name: string; size: number }>('/api/uploads', { method: 'POST', body: file }),

  posts: () => request<{ posts: Post[] }>('/api/posts'),
  createPost: (body: Record<string, any>) => request<{ post: Post }>('/api/posts', { method: 'POST', body }),
  requestPost: (id: string) => request<{ booking: Booking }>(`/api/posts/${id}/request`, { method: 'POST' }),

  aiStatus: () => request<{ configured: boolean; provider: string; model: string; lastOk: any; features: string[] }>('/api/ai/status', { auth: false }),
  copilot: (message: string, history: { role: string; text: string }[] = []) =>
    request<AiReply>('/api/ai/copilot', { method: 'POST', body: { message, history } }),
  brief: (body: Record<string, any>) => request<{ brief: string; provider: string; model: string }>('/api/ai/brief', { method: 'POST', body }),

  resetDemo: () => request<{ ok: boolean }>('/api/demo/reset', { method: 'POST' }),
};

export type { Role };
