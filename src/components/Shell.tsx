import React, { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import {
  Bell, CalendarDays, ChevronDown, Home, LogOut, Search, Settings, Sparkles, Stethoscope,
  UserRound, Wallet, Building2, BriefcaseMedical, ArrowLeftRight,
} from 'lucide-react';
import { useApp, type View } from '../lib/context';
import { cn, initialsOf } from '../lib/utils';
import { Avatar, Badge, Button, Logo } from './ui';

const NAV: { view: View; label: string; icon: React.ReactNode; short: string }[] = [
  { view: 'home', label: 'Dashboard', icon: <Home className="h-[18px] w-[18px]" />, short: 'Home' },
  { view: 'discover', label: 'Discover', icon: <Search className="h-[18px] w-[18px]" />, short: 'Discover' },
  { view: 'bookings', label: 'Bookings', icon: <CalendarDays className="h-[18px] w-[18px]" />, short: 'Bookings' },
  { view: 'wallet', label: 'Wallet', icon: <Wallet className="h-[18px] w-[18px]" />, short: 'Wallet' },
  { view: 'profile', label: 'Profile', icon: <UserRound className="h-[18px] w-[18px]" />, short: 'Profile' },
];

export function Shell({ children }: { children: React.ReactNode }) {
  const { me, view, setView, mode, setMode, setAiOpen, logout, openAuth } = useApp();
  const user = me?.user;
  const unread = me?.unread || 0;
  const [menu, setMenu] = useState(false);
  const [mobileMenu, setMobileMenu] = useState(false);

  useEffect(() => {
    setMenu(false);
  }, [view]);

  if (!user) return <>{children}</>;

  const isBoth = user.role === 'both';
  const roleLabel = mode === 'hire' ? 'Hiring mode' : 'Working mode';

  return (
    <div className="relative min-h-screen bg-[#f4faf7]">
      {/* desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-[248px] flex-col border-r border-white/5 bg-ink-950 text-white lg:flex">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute -left-20 top-10 h-64 w-64 rounded-full bg-brand-500/12 blur-3xl" />
          <div className="absolute -bottom-20 -right-16 h-64 w-64 rounded-full bg-amber-400/8 blur-3xl" />
        </div>
        <div className="relative flex h-16 items-center px-5">
          <Logo dark />
        </div>

        <div className="relative px-4">
          <div className="rounded-2xl border border-white/10 bg-white/[0.04] p-3">
            <div className="flex items-center gap-2">
              <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-500/15 text-brand-400">
                {mode === 'hire' ? <Building2 className="h-4 w-4" /> : <Stethoscope className="h-4 w-4" />}
              </span>
              <div className="min-w-0 flex-1">
                <div className="text-[10px] font-semibold uppercase tracking-[0.12em] text-mint-200/50">Current workspace</div>
                <div className="truncate text-[12px] font-bold">{roleLabel}</div>
              </div>
            </div>
            {isBoth && (
              <button
                onClick={() => setMode(mode === 'hire' ? 'work' : 'hire')}
                className="mt-2.5 flex w-full items-center justify-center gap-1.5 rounded-lg border border-white/10 bg-white/5 py-1.5 text-[10px] font-bold text-mint-100 transition hover:bg-white/10"
              >
                <ArrowLeftRight className="h-3 w-3" /> Switch to {mode === 'hire' ? 'working' : 'hiring'}
              </button>
            )}
          </div>
        </div>

        <nav className="relative mt-5 flex-1 space-y-1 px-3">
          {NAV.map((item) => {
            const active = view === item.view || (view === 'notifications' && item.view === 'home');
            return (
              <button
                key={item.view}
                onClick={() => setView(item.view)}
                className={cn(
                  'group flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-[13px] font-semibold transition',
                  active ? 'bg-white/[0.09] text-white shadow-sm' : 'text-mint-100/55 hover:bg-white/[0.05] hover:text-white',
                )}
              >
                <span className={cn('grid h-8 w-8 place-items-center rounded-lg transition', active ? 'bg-brand-500/20 text-brand-400' : 'text-mint-100/40 group-hover:text-brand-400')}>
                  {item.icon}
                </span>
                {item.label}
              </button>
            );
          })}
        </nav>

        <div className="relative mx-3 mb-3 overflow-hidden rounded-2xl border border-brand-400/20 bg-gradient-to-br from-brand-500/15 via-white/[0.04] to-transparent p-3.5">
          <div className="absolute -right-6 -top-6 h-16 w-16 rounded-full bg-brand-400/20 blur-xl" />
          <div className="relative">
            <div className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.14em] text-brand-400">
              <Sparkles className="h-3 w-3" /> Copilot
            </div>
            <p className="mt-1.5 text-[11px] leading-snug text-mint-100/65">
              {mode === 'hire' ? 'Find cover, draft a brief, or ask about escrow.' : 'Find shifts, check payouts, or plan your week.'}
            </p>
            <button
              onClick={() => setAiOpen(true)}
              className="mt-2.5 flex w-full items-center justify-center gap-1.5 rounded-lg bg-white py-2 text-[11px] font-bold text-ink-950 transition hover:bg-mint-100"
            >
              <Sparkles className="h-3 w-3 text-teal-700" /> Ask MediGig AI
            </button>
          </div>
        </div>

        <div className="relative border-t border-white/8 p-3">
          <button onClick={() => setMenu((v) => !v)} className="flex w-full items-center gap-2.5 rounded-xl px-1.5 py-1.5 text-left transition hover:bg-white/5">
            <Avatar name={user.name} size="sm" />
            <div className="min-w-0 flex-1">
              <div className="truncate text-[12px] font-bold">{user.name}</div>
              <div className="truncate text-[10px] text-mint-100/45">{user.email}</div>
            </div>
            <ChevronDown className={cn('h-3.5 w-3.5 text-mint-100/40 transition', menu && 'rotate-180')} />
          </button>
          <AnimatePresence>
            {menu && (
              <motion.div initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 6 }} className="absolute bottom-16 left-3 right-3 rounded-xl border border-white/10 bg-ink-900 p-1.5 shadow-2xl">
                <button onClick={() => setView('profile')} className="flex w-full items-center gap-2 rounded-lg px-2.5 py-2 text-[11px] font-semibold text-mint-100/80 hover:bg-white/8 hover:text-white">
                  <Settings className="h-3.5 w-3.5" /> Account settings
                </button>
                <button onClick={() => setView('wallet')} className="flex w-full items-center gap-2 rounded-lg px-2.5 py-2 text-[11px] font-semibold text-mint-100/80 hover:bg-white/8 hover:text-white">
                  <Wallet className="h-3.5 w-3.5" /> Wallet & payouts
                </button>
                <button onClick={logout} className="flex w-full items-center gap-2 rounded-lg px-2.5 py-2 text-[11px] font-semibold text-rose-300 hover:bg-rose-500/10">
                  <LogOut className="h-3.5 w-3.5" /> Sign out
                </button>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </aside>

      {/* mobile top bar */}
      <header className="sticky top-0 z-30 border-b border-teal-900/8 glass lg:hidden">
        <div className="flex h-14 items-center justify-between px-4">
          <Logo />
          <div className="flex items-center gap-1.5">
            {isBoth && (
              <button
                onClick={() => setMode(mode === 'hire' ? 'work' : 'hire')}
                className="rounded-full border border-teal-900/10 bg-white/70 p-2 text-teal-700"
                aria-label="Switch mode"
              >
                <ArrowLeftRight className="h-4 w-4" />
              </button>
            )}
            <button onClick={() => setView('notifications')} className="relative rounded-full border border-teal-900/10 bg-white/70 p-2 text-ink-800" aria-label="Notifications">
              <Bell className="h-4 w-4" />
              {unread > 0 && <span className="absolute -right-0.5 -top-0.5 grid h-4 min-w-4 place-items-center rounded-full bg-amber-400 px-1 text-[9px] font-bold text-ink-950">{unread}</span>}
            </button>
            <button onClick={() => setMobileMenu((v) => !v)} aria-label="Account">
              <Avatar name={user.name} size="xs" />
            </button>
          </div>
        </div>
        <AnimatePresence>
          {mobileMenu && (
            <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="overflow-hidden border-t border-teal-900/8 bg-white/90 px-4 py-3">
              <div className="mb-2 text-[11px] font-bold text-ink-950">{user.name}</div>
              <div className="flex gap-2">
                <Button size="xs" variant="soft" onClick={() => { setView('profile'); setMobileMenu(false); }}>Profile</Button>
                <Button size="xs" variant="soft" onClick={() => { setView('wallet'); setMobileMenu(false); }}>Wallet</Button>
                <Button size="xs" variant="outline" onClick={logout}>Sign out</Button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </header>

      {/* desktop top bar */}
      <header className="sticky top-0 z-30 hidden h-16 items-center justify-between border-b border-teal-900/8 glass px-8 lg:flex lg:pl-[272px]">
        <div className="flex items-center gap-2 text-[12px] font-semibold text-slate-500">
          <span className="text-ink-900">{NAV.find((n) => n.view === view)?.label || (view === 'notifications' ? 'Notifications' : 'Dashboard')}</span>
          <span className="text-slate-300">/</span>
          <span className="text-slate-400">{mode === 'hire' ? (me?.hirer?.practiceName || 'Hiring') : (me?.freelancer?.profession || 'Working')}</span>
        </div>
        <div className="flex items-center gap-2.5">
          <Badge tone="teal"><span className="h-1.5 w-1.5 rounded-full bg-teal-500" /> Escrow protected</Badge>
          <button onClick={() => setView('notifications')} className="relative grid h-9 w-9 place-items-center rounded-full border border-teal-900/10 bg-white/70 text-ink-800 transition hover:bg-white" aria-label="Notifications">
            <Bell className="h-4 w-4" />
            {unread > 0 && <span className="absolute -right-0.5 -top-0.5 grid h-4 min-w-4 place-items-center rounded-full bg-amber-400 px-1 text-[9px] font-bold text-ink-950">{unread}</span>}
          </button>
          <button onClick={() => setAiOpen(true)} className="inline-flex items-center gap-1.5 rounded-full bg-ink-900 px-3.5 py-2 text-[11px] font-bold text-white transition hover:bg-ink-800">
            <Sparkles className="h-3.5 w-3.5 text-brand-400" /> Copilot
          </button>
          <Avatar name={user.name} size="sm" />
        </div>
      </header>

      <main className="mx-auto w-full max-w-[1200px] px-4 pb-28 pt-4 sm:px-6 lg:pl-[272px] lg:pr-8 lg:pb-12 lg:pt-6">
        {children}
      </main>

      {/* mobile bottom nav */}
      <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-teal-900/10 glass safe-bottom lg:hidden">
        <div className="mx-auto flex max-w-lg items-end justify-around px-2 pt-1.5">
          {NAV.slice(0, 2).map((item) => <MobileNav key={item.view} item={item} active={view === item.view} onClick={() => setView(item.view)} />)}
          <button
            onClick={() => setAiOpen(true)}
            className="-mt-6 grid h-14 w-14 place-items-center rounded-full bg-ink-900 text-brand-400 shadow-[0_14px_30px_-12px_rgba(4,33,29,.9)] transition active:scale-95"
            aria-label="Open AI copilot"
          >
            <span className="relative">
              <Sparkles className="h-6 w-6" />
              <span className="absolute -right-1 -top-1 h-2 w-2 rounded-full bg-amber-400" />
            </span>
          </button>
          {NAV.slice(2, 4).map((item) => <MobileNav key={item.view} item={item} active={view === item.view} onClick={() => setView(item.view)} />)}
        </div>
      </nav>
    </div>
  );
}

function MobileNav({ item, active, onClick }: { item: { label: string; short: string; icon: React.ReactNode }; active: boolean; onClick: () => void }) {
  return (
    <button onClick={onClick} className={cn('flex w-16 flex-col items-center gap-1 py-1.5 text-[9px] font-semibold transition', active ? 'text-teal-700' : 'text-slate-400')}>
      <span className={cn('grid h-8 w-8 place-items-center rounded-xl transition', active ? 'bg-mint-100 text-teal-700' : 'text-slate-400')}>{item.icon}</span>
      {item.short}
    </button>
  );
}
