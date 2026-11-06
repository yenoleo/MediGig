import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowRight, ArrowUpRight, BadgeCheck, Banknote, CalendarDays, CalendarPlus, Check,
  ChevronRight, Clock3, MapPin, Plus, ShieldCheck, Sparkles, Stethoscope, TrendingUp,
  Wallet, X, Zap,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import {
  addDaysIso, cn, daysBetween, fmtDate, fmtRange, greeting, relativeTime, statusMeta, todayIso,
  zar, zarShort, TIME_SLOTS,
} from '../lib/utils';
import type { Booking, Post, Transaction } from '../lib/types';
import { Avatar, Badge, Button, EmptyState, Field, Input, Modal, Progress, SectionTitle, Select, StatCard, Stars, Textarea } from './ui';
import { EarningsChart, NumberTicker, Reveal, ThinkingState } from './effects';
import { PostShiftModal } from './PostShiftModal';

export function Dashboard() {
  const { me, mode, setView, setAiOpen, refresh, toast, setHireTarget, setFocusBooking } = useApp();
  const user = me?.user;
  const wallet = me?.wallet;

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);
  const [txns, setTxns] = useState<Transaction[]>([]);
  const [earningsByMonth, setEarningsByMonth] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [postOpen, setPostOpen] = useState(false);

  const load = useCallback(async () => {
    try {
      const [b, p, w] = await Promise.all([api.bookings(), api.posts(), api.wallet()]);
      setBookings(b.bookings);
      setPosts(p.posts);
      setTxns(w.transactions);
      setEarningsByMonth(w.earningsByMonth);
    } catch (err: any) {
      toast(err?.message || 'Could not load your workspace.', 'error');
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const act = async (b: Booking, action: string, label: string) => {
    setBusyId(b.id);
    try {
      await api.bookingAction(b.id, action);
      toast(label, 'success');
      await refresh();
      await load();
    } catch (err: any) {
      toast(err?.message || 'That action could not be completed.', 'error');
    } finally {
      setBusyId(null);
    }
  };

  if (!user) return null;

  const mine = bookings.filter((b) => b.hirerId === user.id || b.freelancerId === user.id);
  const isHiring = mode === 'hire';
  const incoming = mine.filter((b) => (isHiring
    ? b.hirerId === user.id && b.status === 'requested'
    : b.freelancerId === user.id && b.status === 'pending_response'));
  const active = mine.filter((b) => ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'requested', 'pending_response'].includes(b.status));
  const completed = mine.filter((b) => ['released', 'closed'].includes(b.status));
  const needsConfirmation = mine.filter((b) => b.hirerId === user.id && b.status === 'completed_by_pro');
  const openShifts = posts.filter((p) => p.status === 'open').slice(0, 4);

  const chart = useMemo(() => {
    const keys = Object.keys(earningsByMonth).sort().slice(-6);
    const out = keys.map((k) => {
      const d = new Date(`${k}-01T12:00:00`);
      return { label: d.toLocaleDateString('en-ZA', { month: 'short' }), value: Math.round((earningsByMonth[k] || 0) / 100) };
    });
    return out.length ? out : [{ label: '—', value: 0 }];
  }, [earningsByMonth]);

  const availability = me?.freelancer?.availableDates || [];
  const week = Array.from({ length: 7 }, (_, i) => {
    const iso = addDaysIso(todayIso(), i);
    return { iso, on: availability.includes(iso) };
  });

  const toggleDay = async (iso: string) => {
    if (!me?.freelancer) return;
    const next = week.find((d) => d.iso === iso)?.on
      ? availability.filter((d) => d !== iso)
      : [...availability, iso].sort();
    try {
      await api.updateFreelancer({ availableDates: next });
      await refresh();
    } catch (err: any) {
      toast(err?.message || 'Could not update availability.', 'error');
    }
  };

  const profilePct = (() => {
    let done = 0;
    const total = isHiring ? 4 : 6;
    if (user.name) done++;
    if (user.phone) done++;
    if (isHiring) {
      if (me?.hirer?.practiceName) done++;
      if (me?.hirer?.registrationNumber) done++;
      if (me?.wallet && me.wallet.balanceCents > 0) done++;
    } else {
      if (me?.freelancer?.bio) done++;
      if ((me?.freelancer?.certifications || []).length) done++;
      if (me?.freelancer?.dailyRateCents) done++;
      if (availability.length) done++;
    }
    return Math.round((done / total) * 100);
  })();

  return (
    <div className="space-y-6">
      {/* header */}
      <Reveal>
        <div className="relative overflow-hidden rounded-[26px] border border-teal-900/10 bg-ink-950 px-5 py-5 text-white sm:px-7 sm:py-6">
          <div className="pointer-events-none absolute -right-16 -top-20 h-64 w-64 rounded-full bg-brand-500/20 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-24 left-1/3 h-56 w-56 rounded-full bg-amber-400/10 blur-3xl" />
          <div className="relative flex flex-wrap items-start justify-between gap-5">
            <div>
              <div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.18em] text-mint-200/60">
                <span className="h-1.5 w-1.5 rounded-full bg-brand-400" />
                {new Date().toLocaleDateString('en-ZA', { weekday: 'long', day: 'numeric', month: 'long' })}
              </div>
              <h1 className="mt-2 text-2xl font-extrabold tracking-tight sm:text-[30px]">
                {greeting()}, {user.name.replace(/^(Dr\.?|Prof\.?)\s+/i, '').split(' ')[0]}.
              </h1>
              <p className="mt-1 max-w-lg text-[13px] leading-relaxed text-mint-100/65">
                {isHiring
                  ? `${incoming.length ? `${incoming.length} shift request${incoming.length > 1 ? 's' : ''} waiting on you` : 'No pending requests'} · ${active.length} active booking${active.length === 1 ? '' : 's'} · escrow is covering every confirmed shift.`
                  : `${active.length} active gig${active.length === 1 ? '' : 's'} · ${openShifts.length} open shifts near you · payouts land 5 business days after completion.`}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button size="sm" variant="white" onClick={() => (isHiring ? setView('discover') : setView('discover'))} icon={<ArrowRight className="h-3.5 w-3.5" />}>
                {isHiring ? 'Find cover' : 'Find shifts'}
              </Button>
              {isHiring ? (
                <Button size="sm" className="bg-brand-500 hover:bg-brand-600" onClick={() => setPostOpen(true)} icon={<Plus className="h-3.5 w-3.5" />}>
                  Post a shift
                </Button>
              ) : (
                <Button size="sm" className="bg-brand-500 hover:bg-brand-600" onClick={() => setAiOpen(true)} icon={<Sparkles className="h-3.5 w-3.5" />}>
                  Ask copilot
                </Button>
              )}
            </div>
          </div>
        </div>
      </Reveal>

      {/* stats */}
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          label={isHiring ? 'Wallet available' : 'Available to withdraw'}
          value={<NumberTicker value={(wallet?.balanceCents || 0) / 100} format={(n) => zar(Math.round(n) * 100)} />}
          sub={isHiring ? 'Top up to fund escrow' : 'Escrow releases to this balance'}
          icon={<Wallet className="h-4 w-4" />}
          tone="dark"
        />
        <StatCard
          label={isHiring ? 'Held in escrow' : 'Pending payout'}
          value={zar(isHiring ? wallet?.escrowHeldCents || 0 : wallet?.escrowPendingCents || 0)}
          sub={isHiring ? 'Released on confirmation' : 'Clears 5 business days'}
          icon={<ShieldCheck className="h-4 w-4" />}
          tone="mint"
        />
        {isHiring ? (
          <StatCard label="Requests to review" value={String(incoming.length)} sub={incoming.length ? 'Needs a decision today' : 'All caught up'} icon={<Zap className="h-4 w-4" />} tone="sand" />
        ) : (
          <StatCard label="This month" value={zar(Object.entries(earningsByMonth).filter(([k]) => k === todayIso().slice(0, 7)).reduce((s, [, v]) => s + v, 0))} sub={`${completed.length} completed gig${completed.length === 1 ? '' : 's'} all time`} icon={<TrendingUp className="h-4 w-4" />} tone="sand" />
        )}
        <StatCard label={isHiring ? 'Active bookings' : 'Upcoming shifts'} value={String(active.length)} sub={`${mine.length} total bookings`} icon={<CalendarDays className="h-4 w-4" />} tone="white" />
      </div>

      <div className="grid gap-5 lg:grid-cols-[1.45fr_1fr]">
        <div className="space-y-5">
          {/* needs attention */}
          <section className="card p-4 sm:p-5">
            <SectionTitle
              eyebrow={isHiring ? 'Action needed' : 'Requests'}
              title={isHiring ? 'Shift requests waiting on you' : 'Hirer invitations'}
              sub={isHiring ? 'Approve to hold escrow and unlock the full address for the professional.' : 'Accepting confirms the booking and releases the practice address to you.'}
              action={<button onClick={() => setView('bookings')} className="inline-flex items-center gap-1 text-[11px] font-bold text-teal-700 hover:underline">All bookings <ChevronRight className="h-3 w-3" /></button>}
            />
            {incoming.length === 0 ? (
              <EmptyState
                icon={<Check className="h-5 w-5" />}
                title={isHiring ? 'No requests pending' : 'No invitations right now'}
                body={isHiring ? 'When a professional requests one of your open shifts it will appear here with their rate and distance.' : 'Open shifts near you are listed below — request one and the practice will confirm.'}
              />
            ) : (
              <div className="space-y-2.5">
                {incoming.map((b) => (
                  <div key={b.id} className="rounded-2xl border border-teal-900/10 bg-white p-3.5">
                    <div className="flex items-start gap-3">
                      <Avatar name={b.counterpart?.name || 'MediGig'} size="md" />
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-1.5">
                          <span className="text-[13px] font-bold text-ink-950">{b.counterpart?.name || 'Professional'}</span>
                          {b.counterpart?.verified && <BadgeCheck className="h-3.5 w-3.5 text-teal-600" />}
                          <Badge tone="amber" className="ml-auto">{zar(b.totalCents)}</Badge>
                        </div>
                        <div className="mt-0.5 text-[11px] text-slate-500">{b.roleTitle}</div>
                        <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-[10px] text-slate-400">
                          <span className="inline-flex items-center gap-1"><CalendarDays className="h-3 w-3" />{fmtRange(b.dateStart, b.dateEnd)}</span>
                          <span className="inline-flex items-center gap-1"><Clock3 className="h-3 w-3" />{b.timeSlot}</span>
                          <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />{b.distanceKm} km</span>
                        </div>
                      </div>
                    </div>
                    <div className="mt-3 flex gap-2">
                      {isHiring ? (
                        <>
                          <Button
                            size="xs" className="flex-1 bg-brand-600 hover:bg-brand-700"
                            loading={busyId === b.id}
                            onClick={() => act(b, 'hirer-accept', 'Shift approved — escrow funded.')}
                            icon={<Check className="h-3 w-3" />}
                          >
                            Approve · hold escrow
                          </Button>
                          <Button size="xs" variant="outline" disabled={busyId === b.id} onClick={() => act(b, 'hirer-decline', 'Request declined.')} icon={<X className="h-3 w-3" />}>
                            Decline
                          </Button>
                        </>
                      ) : (
                        <>
                          <Button
                            size="xs" className="flex-1 bg-brand-600 hover:bg-brand-700"
                            loading={busyId === b.id}
                            onClick={() => act(b, 'accept', 'Booking accepted — address unlocked.')}
                            icon={<Check className="h-3 w-3" />}
                          >
                            Accept shift
                          </Button>
                          <Button size="xs" variant="outline" disabled={busyId === b.id} onClick={() => act(b, 'decline', 'Booking declined.')} icon={<X className="h-3 w-3" />}>
                            Decline
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* earnings / active */}
          <section className="card p-4 sm:p-5">
            <SectionTitle
              eyebrow={isHiring ? 'Spend' : 'Earnings'}
              title={isHiring ? 'Escrow commitment' : 'Released payouts'}
              sub={isHiring ? 'Every confirmed booking is funded up front — no invoices to chase.' : 'Escrow clears 5 business days after the hirer confirms completion.'}
              action={<Badge tone={isHiring ? 'sand' : 'emerald'}>{isHiring ? `5% service fee` : 'No fees on payouts'}</Badge>}
            />
            <div className="grid gap-4 sm:grid-cols-[1.2fr_1fr]">
              <div className="rounded-2xl border border-teal-900/8 bg-white/70 p-3">
                <div className="mb-1 flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">{isHiring ? 'Escrow held by month' : 'Payouts by month'}</span>
                  <span className="font-mono text-[10px] text-teal-700">ZAR</span>
                </div>
                <EarningsChart data={chart} height={110} format={(n) => zar(n * 100)} />
              </div>
              <div className="space-y-2">
                {(isHiring
                  ? txns.filter((t) => t.type === 'escrow_hold').slice(0, 3)
                  : txns.filter((t) => t.type === 'escrow_release').slice(0, 3)
                ).map((t) => (
                  <div key={t.id} className="flex items-center gap-2.5 rounded-xl border border-teal-900/8 bg-white/70 px-2.5 py-2">
                    <span className={cn('grid h-7 w-7 shrink-0 place-items-center rounded-lg', t.amountCents < 0 ? 'bg-amber-50 text-amber-700' : 'bg-emerald-50 text-emerald-700')}>
                      <Banknote className="h-3.5 w-3.5" />
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-[11px] font-semibold text-ink-900">{t.description}</div>
                      <div className="text-[9px] text-slate-400">{relativeTime(t.createdAt)} · {t.ref}</div>
                    </div>
                    <span className={cn('text-[11px] font-bold', t.amountCents < 0 ? 'text-ink-900' : 'text-emerald-700')}>{zar(Math.abs(t.amountCents))}</span>
                  </div>
                ))}
                {!txns.length && <div className="rounded-xl border border-dashed border-teal-900/15 p-3 text-center text-[11px] text-slate-400">No transactions yet.</div>}
              </div>
            </div>
          </section>

          {/* open shifts */}
          <section className="card p-4 sm:p-5">
            <SectionTitle
              eyebrow={isHiring ? 'Your open shifts' : 'Near you'}
              title={isHiring ? 'Shifts you have posted' : 'Open shifts you can pick up'}
              sub={isHiring ? 'Professionals can request these directly — you approve and escrow is funded.' : 'Request a shift and the practice confirms. Address is shared after approval.'}
              action={isHiring
                ? <Button size="xs" variant="soft" onClick={() => setPostOpen(true)} icon={<Plus className="h-3 w-3" />}>Post</Button>
                : <button onClick={() => setView('discover')} className="inline-flex items-center gap-1 text-[11px] font-bold text-teal-700 hover:underline">Browse all <ChevronRight className="h-3 w-3" /></button>}
            />
            <div className="space-y-2">
              {(isHiring ? posts.filter((p) => p.hirerId === user.id) : openShifts).slice(0, 4).map((p) => (
                <div key={p.id} className="flex flex-wrap items-center gap-3 rounded-xl border border-teal-900/8 bg-white/70 p-3">
                  <span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-mint-100 text-teal-700"><Stethoscope className="h-4 w-4" /></span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[12px] font-bold text-ink-950">{p.title}</div>
                    <div className="truncate text-[10px] text-slate-500">{p.profession}{p.specialty ? ` · ${p.specialty}` : ''} · {p.city}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-[12px] font-extrabold text-ink-950">{zar(p.rateCents)}</div>
                    <div className="text-[9px] text-slate-400">/ day · {fmtDate(p.dateStart, { year: false })}</div>
                  </div>
                  {!isHiring && (
                    <Button size="xs" variant="brand" onClick={async () => {
                      try {
                        await api.requestPost(p.id);
                        toast('Request sent — the practice will confirm.', 'success');
                        await load();
                      } catch (err: any) {
                        toast(err?.message || 'Could not request this shift.', 'error');
                      }
                    }}>Request</Button>
                  )}
                  {isHiring && <Badge tone="teal">{p.status}</Badge>}
                </div>
              ))}
              {(isHiring ? posts.filter((p) => p.hirerId === user.id) : openShifts).length === 0 && (
                <EmptyState icon={<CalendarPlus className="h-5 w-5" />} title={isHiring ? 'No open shifts posted' : 'No open shifts right now'} body={isHiring ? 'Post a shift and let verified professionals request it. AI can draft the brief for you.' : 'Try widening your search radius or check back later today.'} action={isHiring ? <Button size="sm" onClick={() => setPostOpen(true)}>Post a shift</Button> : undefined} />
              )}
            </div>
          </section>
        </div>

        {/* right rail */}
        <div className="space-y-5">
          {!isHiring && (
            <section className="card p-4 sm:p-5">
              <SectionTitle eyebrow="Availability" title="Your week" sub="Only dates you mark available can be booked." />
              <div className="grid grid-cols-7 gap-1.5">
                {week.map((d) => {
                  const dt = new Date(`${d.iso}T12:00:00`);
                  const active = d.on;
                  return (
                    <button
                      key={d.iso}
                      onClick={() => toggleDay(d.iso)}
                      className={cn(
                        'flex flex-col items-center gap-1 rounded-xl border py-2 transition',
                        active ? 'border-teal-600 bg-mint-100 text-ink-900 shadow-sm' : 'border-teal-900/10 bg-white/70 text-slate-400 hover:border-teal-900/25',
                      )}
                    >
                      <span className="text-[9px] font-bold uppercase">{dt.toLocaleDateString('en-ZA', { weekday: 'short' }).slice(0, 2)}</span>
                      <span className="text-[13px] font-extrabold">{dt.getDate()}</span>
                      <span className={cn('h-1.5 w-1.5 rounded-full', active ? 'bg-teal-600' : 'bg-slate-300')} />
                    </button>
                  );
                })}
              </div>
              <div className="mt-3 flex items-center justify-between rounded-xl bg-mint-50 px-3 py-2 text-[10px] text-teal-800">
                <span>{availability.length} available days in total</span>
                <button onClick={() => setView('profile')} className="font-bold hover:underline">Manage</button>
              </div>
            </section>
          )}

          <section className="relative overflow-hidden rounded-[24px] border border-teal-900/10 bg-gradient-to-br from-mint-100 via-white to-sand-50 p-4 sm:p-5">
            <div className="flex items-start justify-between">
              <div>
                <div className="text-[10px] font-bold uppercase tracking-[0.16em] text-brand-700/70">Wallet</div>
                <div className="mt-1 text-[26px] font-extrabold leading-none tracking-tight text-ink-950">{zar(wallet?.balanceCents || 0, { decimals: true })}</div>
                <div className="mt-1 text-[11px] text-slate-500">available balance</div>
              </div>
              <span className="grid h-10 w-10 place-items-center rounded-2xl bg-ink-900 text-brand-400"><Wallet className="h-4.5 w-4.5" /></span>
            </div>
            <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-teal-900/10">
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${Math.min(100, ((wallet?.balanceCents || 0) / Math.max(1, (wallet?.balanceCents || 0) + (wallet?.escrowCents || 0))) * 100)}%` }}
                transition={{ duration: 0.9 }}
                className="h-full rounded-full bg-gradient-to-r from-teal-500 to-emerald-400"
              />
            </div>
            <div className="mt-1.5 flex justify-between text-[9px] text-slate-400">
              <span>available</span>
              <span>{zar(wallet?.escrowCents || 0)} in escrow</span>
            </div>
            <div className="mt-3 flex gap-2">
              <Button size="xs" variant="primary" className="flex-1" onClick={() => setView('wallet')}>Wallet</Button>
              {isHiring && <Button size="xs" variant="white" className="flex-1" onClick={() => setView('wallet')} icon={<Plus className="h-3 w-3" />}>Top up</Button>}
            </div>
          </section>

          {needsConfirmation.length > 0 && (
            <section className="card border-amber-200 bg-amber-50/70 p-4 sm:p-5">
              <div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.16em] text-amber-800">
                <ShieldCheck className="h-3.5 w-3.5" /> Confirmation needed
              </div>
              <p className="mt-2 text-[12px] leading-relaxed text-amber-900/80">
                {needsConfirmation.length === 1 ? 'A professional marked a shift complete.' : `${needsConfirmation.length} professionals marked shifts complete.`} Confirm to start the 5-day release timer.
              </p>
              <div className="mt-3 space-y-2">
                {needsConfirmation.slice(0, 2).map((b) => (
                  <button key={b.id} onClick={() => { setFocusBooking(b.id); setView('bookings'); }} className="flex w-full items-center gap-2 rounded-xl border border-amber-200 bg-white px-2.5 py-2 text-left transition hover:border-amber-300">
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-[11px] font-bold text-ink-900">{b.roleTitle}</div>
                      <div className="text-[9px] text-slate-400">{b.ref} · {zar(b.escrowCents)} in escrow</div>
                    </div>
                    <ArrowUpRight className="h-3.5 w-3.5 text-amber-600" />
                  </button>
                ))}
              </div>
            </section>
          )}

          <section className="card p-4 sm:p-5">
            <SectionTitle eyebrow="Profile strength" title="Complete your workspace" sub={profilePct === 100 ? 'Everything is in place.' : 'A complete profile gets better matches.'} />
            <div className="flex items-center gap-3">
              <div className="relative grid h-16 w-16 place-items-center">
                <svg width="64" height="64" className="-rotate-90">
                  <circle cx="32" cy="32" r="27" fill="none" stroke="rgba(15,118,110,.12)" strokeWidth="7" />
                  <motion.circle cx="32" cy="32" r="27" fill="none" stroke="#0d9488" strokeWidth="7" strokeLinecap="round" strokeDasharray={2 * Math.PI * 27} initial={{ strokeDashoffset: 2 * Math.PI * 27 }} animate={{ strokeDashoffset: 2 * Math.PI * 27 * (1 - profilePct / 100) }} transition={{ duration: 1 }} />
                </svg>
                <span className="absolute text-[13px] font-extrabold text-ink-950">{profilePct}%</span>
              </div>
              <div className="flex-1 space-y-1.5">
                {(isHiring
                  ? [
                      { k: 'Contact details', ok: !!user.phone },
                      { k: 'Practice name', ok: !!me?.hirer?.practiceName },
                      { k: 'BHF / registration', ok: !!me?.hirer?.registrationNumber },
                      { k: 'Wallet funded', ok: (wallet?.balanceCents || 0) > 0 },
                    ]
                  : [
                      { k: 'Contact details', ok: !!user.phone },
                      { k: 'Professional bio', ok: !!me?.freelancer?.bio },
                      { k: 'Certification on file', ok: (me?.freelancer?.certifications || []).length > 0 },
                      { k: 'Day rate set', ok: !!me?.freelancer?.dailyRateCents },
                      { k: 'Availability marked', ok: availability.length > 0 },
                    ]
                ).map((x) => (
                  <div key={x.k} className="flex items-center gap-1.5 text-[11px]">
                    <span className={cn('grid h-4 w-4 place-items-center rounded-full', x.ok ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-400')}>
                      {x.ok ? <Check className="h-2.5 w-2.5" /> : <span className="h-1 w-1 rounded-full bg-current" />}
                    </span>
                    <span className={x.ok ? 'text-slate-500 line-through decoration-slate-300' : 'font-medium text-ink-800'}>{x.k}</span>
                  </div>
                ))}
              </div>
            </div>
            <Button size="sm" variant="outline" className="mt-3 w-full" onClick={() => setView('profile')}>Complete profile</Button>
          </section>

          <section className="relative overflow-hidden rounded-[24px] bg-ink-950 p-4 text-white sm:p-5">
            <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-brand-500/20 blur-3xl" />
            <div className="relative">
              <div className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.16em] text-brand-400"><Sparkles className="h-3 w-3" /> MediGig Copilot</div>
              <p className="mt-2 text-[12px] leading-relaxed text-mint-100/70">
                {isHiring ? 'Ask for “an ICU nurse tomorrow night under R800” — the copilot searches the marketplace and drafts the brief.' : 'Ask which shifts suit your certifications this week, or when your next payout lands.'}
              </p>
              <Button size="sm" variant="white" className="mt-3 w-full" onClick={() => setAiOpen(true)} icon={<Sparkles className="h-3.5 w-3.5 text-teal-700" />}>Open copilot</Button>
            </div>
          </section>
        </div>
      </div>

      <PostShiftModal open={postOpen} onClose={() => setPostOpen(false)} onCreated={() => { setPostOpen(false); load(); }} />
    </div>
  );
}
