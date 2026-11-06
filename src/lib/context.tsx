import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { api, getToken, setToken } from './api';
import type { MeResponse, ProfessionalCard, Role } from './types';
import type { Mode } from './types';

export type View = 'home' | 'discover' | 'bookings' | 'wallet' | 'profile' | 'notifications';
export type ToastTone = 'success' | 'error' | 'info';

export interface Toast {
  id: string;
  text: string;
  tone: ToastTone;
}

interface AppState {
  me: MeResponse | null;
  loading: boolean;
  mode: Mode;
  setMode: (m: Mode) => void;
  view: View;
  setView: (v: View) => void;
  focusBooking: string | null;
  setFocusBooking: (id: string | null) => void;
  authOpen: boolean;
  authRole: Role;
  authIntent: 'signin' | 'signup';
  openAuth: (opts?: { role?: Role; intent?: 'signin' | 'signup' }) => void;
  closeAuth: () => void;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: Record<string, any>) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<MeResponse | null>;
  signedIn: boolean;
  toasts: Toast[];
  toast: (text: string, tone?: ToastTone) => void;
  dismissToast: (id: string) => void;
  aiOpen: boolean;
  setAiOpen: (v: boolean) => void;
  hireTarget: ProfessionalCard | null;
  setHireTarget: (p: ProfessionalCard | null) => void;
  topUpOpen: boolean;
  setTopUpOpen: (v: boolean) => void;
}

const Ctx = createContext<AppState | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [me, setMe] = useState<MeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState<Mode>('hire');
  const [view, setView] = useState<View>('home');
  const [focusBooking, setFocusBooking] = useState<string | null>(null);
  const [authOpen, setAuthOpen] = useState(false);
  const [authRole, setAuthRole] = useState<Role>('freelancer');
  const [authIntent, setAuthIntent] = useState<'signin' | 'signup'>('signup');
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [aiOpen, setAiOpen] = useState(false);
  const [hireTarget, setHireTarget] = useState<ProfessionalCard | null>(null);
  const [topUpOpen, setTopUpOpen] = useState(false);
  const toast = useCallback((text: string, tone: ToastTone = 'info') => {
    const id = Math.random().toString(36).slice(2);
    setToasts((t) => [...t.slice(-2), { id, text, tone }]);
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 4600);
  }, []);

  const dismissToast = useCallback((id: string) => setToasts((t) => t.filter((x) => x.id !== id)), []);

  const refresh = useCallback(async () => {
    const token = getToken();
    if (!token) {
      setMe(null);
      return null;
    }
    try {
      const data = await api.me();
      if (!data?.user) {
        setToken(null);
        setMe(null);
        return null;
      }
      setMe(data);
      return data;
    } catch (err: any) {
      if (err?.status === 401 || err?.status === 403) {
        setToken(null);
        setMe(null);
      }
      return null;
    }
  }, []);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        await refresh();
      } catch (err) {
        console.error('Session initialization error:', err);
      } finally {
        if (active) setLoading(false);
      }
    })();
    // Failsafe: never allow loading screen to hang indefinitely
    const timer = setTimeout(() => {
      if (active) setLoading(false);
    }, 2500);
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [refresh]);

  useEffect(() => {
    if (!me?.user) return;
    setMode(me.user.role === 'hirer' ? 'hire' : me.user.role === 'freelancer' ? 'work' : 'hire');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [me?.user?.id]);

  // light polling so notifications and escrow timers stay fresh
  useEffect(() => {
    if (!me?.user) return;
    const t = setInterval(() => { refresh(); }, 60000);
    return () => clearInterval(t);
  }, [me?.user?.id, refresh]);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.login(email, password);
    setToken(res.token);
    const data = await api.me();
    setMe(data);
    setView('home');
    setAuthOpen(false);
    toast(`Welcome back, ${res.user.name.split(' ')[0]}.`, 'success');
  }, [toast]);

  const register = useCallback(async (payload: Record<string, any>) => {
    const res = await api.register(payload);
    setToken(res.token);
    const data = await api.me();
    setMe(data);
    setView('home');
    setAuthOpen(false);
    toast('Account created — your workspace is ready.', 'success');
  }, [toast]);

  const logout = useCallback(async () => {
    try { await api.logout(); } catch {}
    setToken(null);
    setMe(null);
    setView('home');
    setAiOpen(false);
    toast('Signed out.', 'info');
  }, [toast]);

  const openAuth = useCallback((opts?: { role?: Role; intent?: 'signin' | 'signup' }) => {
    if (opts?.role) setAuthRole(opts.role);
    setAuthIntent(opts?.intent || 'signup');
    setAuthOpen(true);
  }, []);
  const closeAuth = useCallback(() => setAuthOpen(false), []);

  const value = useMemo<AppState>(() => ({
    me, loading, mode, setMode, view, setView, focusBooking, setFocusBooking,
    authOpen, authRole, authIntent, openAuth, closeAuth, login, register, logout, refresh,
    signedIn: !!me?.user, toasts, toast, dismissToast, aiOpen, setAiOpen, hireTarget, setHireTarget,
    topUpOpen, setTopUpOpen,
  }), [me, loading, mode, view, focusBooking, authOpen, authRole, authIntent, openAuth, closeAuth, login, register, logout, refresh, toasts, toast, dismissToast, aiOpen, hireTarget, topUpOpen]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useApp() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useApp must be used inside AppProvider');
  return ctx;
}
