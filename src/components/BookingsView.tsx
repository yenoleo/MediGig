import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowUpRight, BadgeCheck, CalendarDays, Check, CheckCircle2, Clock3, MapPin, Phone,
  Play, RotateCcw, ShieldAlert, ShieldCheck, Star, Timer, X, Copy,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { cn, fmtDate, fmtRange, fmtTime, relativeTime, statusMeta, zar } from '../lib/utils';
import type { Booking } from '../lib/types';
import { Avatar, Badge, Button, EmptyState, Modal, Progress, Segmented, Stars, Textarea } from './ui';

type Tab = 'active' | 'past' | 'all';
const ACTIVE = ['requested', 'pending_response', 'accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'disputed'];
const PAST = ['released', 'closed', 'declined', 'cancelled'];

export function BookingsView() {
  const { me, refresh, toast, focusBooking, setFocusBooking } = useApp();
  const user = me?.user;
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<Tab>('active');
  const [detailId, setDetailId] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [ratingFor, setRatingFor] = useState<Booking | null>(null);
  const [stars, setStars] = useState(5);
  const [review, setReview] = useState('');

  const load = useCallback(async () => {
    try {
      const res = await api.bookings();
      setBookings(res.bookings);
      return res.bookings;
    } catch (err: any) {
      toast(err?.message || 'Could not load bookings.', 'error');
      return [];
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    if (!focusBooking) return;
    setDetailId(focusBooking);
    setFocusBooking(null);
  }, [focusBooking, setFocusBooking]);

  const detail = bookings.find((b) => b.id === detailId) || null;

  const act = async (b: Booking, action: string, label: string, body: Record<string, any> = {}) => {
    setBusy(b.id);
    try {
      await api.bookingAction(b.id, action, body);
      toast(label, 'success');
      const next = await load();
      await refresh();
      if (detailId && next.every((x) => x.id !== detailId)) setDetailId(null);
    } catch (err: any) {
      toast(err?.message || 'That action could not be completed.', 'error');
    } finally {
      setBusy(null);
    }
  };

  const copyShiftDetails = (b: Booking) => {
    const text = `Shift: ${b.roleTitle}\nRef: ${b.ref}\nDates: ${fmtRange(b.dateStart, b.dateEnd)}\nTime: ${b.timeSlot}\nLocation: ${b.location}\nCity: ${b.city}`;
    const fallbackCopy = (str: string) => {
      const el = document.createElement('textarea');
      el.value = str;
      el.style.position = 'fixed';
      el.style.opacity = '0';
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      toast('Shift details copied to clipboard.', 'success');
    };

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(() => {
        toast('Shift details copied to clipboard.', 'success');
      }).catch(() => fallbackCopy(text));
    } else {
      fallbackCopy(text);
    }
  };

  const list = useMemo(() => {
    const mine = bookings.filter((b) => !user?.id || b.hirerId === user.id || b.freelancerId === user.id);
    if (tab === 'active') return mine.filter((b) => ACTIVE.includes(b.status));
    if (tab === 'past') return mine.filter((b) => PAST.includes(b.status));
    return mine;
  }, [bookings, tab, user?.id]);

  const counts = useMemo(() => {
    const mine = bookings.filter((b) => !user?.id || b.hirerId === user.id || b.freelancerId === user.id);
    return {
      active: mine.filter((b) => ACTIVE.includes(b.status)).length,
      past: mine.filter((b) => PAST.includes(b.status)).length,
      all: mine.length,
    };
  }, [bookings, user?.id]);

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <div className="text-[10px] font-bold uppercase tracking-[0.18em] text-brand-700/70">Bookings</div>
          <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-ink-950 sm:text-3xl">Every shift, tracked.</h1>
          <p className="mt-1 max-w-xl text-[12px] text-slate-600">Escrow status, addresses and the full activity trail live here.</p>
        </div>
        <Segmented
          value={tab}
          onChange={setTab}
          options={[
            { value: 'active', label: `Active (${counts.active})` },
            { value: 'past', label: `Past (${counts.past})` },
            { value: 'all', label: `All (${counts.all})` },
          ]}
        />
      </div>

      {loading ? (
        <div className="space-y-3">{Array.from({ length: 3 }).map((_, i) => <div key={i} className="shimmer h-28 rounded-2xl" />)}</div>
      ) : list.length === 0 ? (
        <EmptyState
          icon={<CalendarDays className="h-5 w-5" />}
          title={tab === 'active' ? 'No active bookings' : 'Nothing here yet'}
          body={tab === 'active' ? 'Book a professional or request an open shift — confirmed bookings appear here with their escrow status.' : 'Completed, declined and cancelled bookings are archived here.'}
        />
      ) : (
        <div className="space-y-3">
          {list.map((b) => {
            const isHirer = b.hirerId === user?.id;
            const meta = statusMeta[b.status] || statusMeta.requested;
            const other = b.counterpart;
            const waitingOnYou =
              (isHirer && b.status === 'requested') ||
              (!isHirer && b.status === 'pending_response') ||
              (isHirer && b.status === 'completed_by_pro');
            return (
              <motion.div key={b.id} layout initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className={cn('card overflow-hidden', waitingOnYou && 'border-amber-200/80 ring-1 ring-amber-200/40')}>
                <div className="p-4">
                  <div className="flex flex-wrap items-start gap-3">
                    <Avatar name={other?.name || 'MediGig'} size="md" />
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-1.5">
                        <span className="truncate text-[13px] font-bold text-ink-950">{b.roleTitle}</span>
                        <Badge tone={meta.tone.includes('amber') ? 'amber' : meta.tone.includes('emerald') ? 'emerald' : meta.tone.includes('violet') ? 'violet' : meta.tone.includes('sky') ? 'sky' : meta.tone.includes('indigo') ? 'sky' : meta.tone.includes('rose') ? 'rose' : 'teal'}>
                          <span className={cn('h-1.5 w-1.5 rounded-full', meta.dot)} /> {meta.label}
                        </Badge>
                        {waitingOnYou && <Badge tone="amber">Action needed</Badge>}
                      </div>
                      <div className="mt-0.5 text-[11px] text-slate-500">
                        {isHirer ? `You hired ${other?.name}` : `${other?.name} hired you`} · {b.ref}
                      </div>
                      <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-[10px] text-slate-400">
                        <span className="inline-flex items-center gap-1"><CalendarDays className="h-3 w-3" />{fmtRange(b.dateStart, b.dateEnd)}</span>
                        <span className="inline-flex items-center gap-1"><Clock3 className="h-3 w-3" />{b.timeSlot}</span>
                        <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />{b.distanceKm} km · {b.city}</span>
                        <span className="inline-flex items-center gap-1"><ShieldCheck className="h-3 w-3" />{b.escrowCents ? `${zar(b.escrowCents)} in escrow` : 'No escrow held'}</span>
                      </div>
                    </div>
                    <div className="shrink-0 text-right">
                      <div className="text-[15px] font-extrabold text-ink-950">{zar(b.totalCents)}</div>
                      <div className="text-[9px] text-slate-400">{b.days} day{b.days > 1 ? 's' : ''} · incl. {zar(b.serviceFeeCents)} fee</div>
                    </div>
                  </div>

                  <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-teal-900/6 pt-3">
                    <Button size="xs" variant="outline" onClick={() => setDetailId(b.id)} icon={<ArrowUpRight className="h-3 w-3" />}>Details</Button>
                    <Button size="xs" variant="ghost" onClick={() => copyShiftDetails(b)} icon={<Copy className="h-3 w-3" />}>Copy details</Button>

                    {isHirer && b.status === 'requested' && (
                      <>
                        <Button size="xs" variant="brand" loading={busy === b.id} onClick={() => act(b, 'hirer-accept', 'Shift approved — escrow funded.')} icon={<Check className="h-3 w-3" />}>Approve & fund escrow</Button>
                        <Button size="xs" variant="ghost" disabled={busy === b.id} onClick={() => act(b, 'hirer-decline', 'Request declined.')}>Decline</Button>
                      </>
                    )}

                    {!isHirer && b.status === 'pending_response' && (
                      <>
                        <Button size="xs" variant="brand" loading={busy === b.id} onClick={() => act(b, 'accept', 'Booking accepted — address unlocked.')} icon={<Check className="h-3 w-3" />}>Accept</Button>
                        <Button size="xs" variant="ghost" disabled={busy === b.id} onClick={() => act(b, 'decline', 'Booking declined.')}>Decline</Button>
                      </>
                    )}

                    {b.status === 'accepted' && (
                      <Button size="xs" variant="soft" disabled={busy === b.id} onClick={() => act(b, 'start', 'Shift started.')} icon={<Play className="h-3 w-3" />}>Start shift</Button>
                    )}

                    {!isHirer && ['accepted', 'in_progress'].includes(b.status) && (
                      <Button size="xs" variant="brand" loading={busy === b.id} onClick={() => act(b, 'complete', 'Marked complete — waiting on hirer confirmation.')} icon={<CheckCircle2 className="h-3 w-3" />}>Mark complete</Button>
                    )}

                    {isHirer && b.status === 'completed_by_pro' && (
                      <Button size="xs" variant="amber" loading={busy === b.id} onClick={() => act(b, 'confirm', 'Confirmed — release scheduled in 5 business days.')} icon={<Timer className="h-3 w-3" />}>Confirm completion</Button>
                    )}

                    {b.status === 'pending_release' && (
                      <>
                        <Badge tone="violet"><Timer className="h-3 w-3" /> Release {b.releaseAt ? fmtDate(b.releaseAt) : 'in 5 days'}</Badge>
                        <Button size="xs" variant="soft" loading={busy === b.id} onClick={() => act(b, 'release', 'Escrow released to the professional.')} icon={<Check className="h-3 w-3" />}>Release now</Button>
                        {isHirer && <Button size="xs" variant="ghost" disabled={busy === b.id} onClick={() => act(b, 'dispute', 'Dispute opened — release paused.')} icon={<ShieldAlert className="h-3 w-3" />}>Dispute</Button>}
                      </>
                    )}

                    {isHirer && b.status === 'released' && (
                      <Button size="xs" variant="brand" onClick={() => { setRatingFor(b); setStars(5); setReview(''); }} icon={<Star className="h-3 w-3" />}>Rate professional</Button>
                    )}

                    {['requested', 'pending_response', 'accepted'].includes(b.status) && (
                      <Button size="xs" variant="ghost" disabled={busy === b.id} onClick={() => act(b, 'cancel', 'Booking cancelled — escrow returned.')} icon={<X className="h-3 w-3" />}>Cancel</Button>
                    )}

                    {b.rating && (
                      <span className="ml-auto inline-flex items-center gap-1 text-[10px] text-slate-400"><Stars value={b.rating} size={10} /> {b.rating}.0</span>
                    )}
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}

      {/* detail modal */}
      <Modal
        open={!!detail}
        onClose={() => setDetailId(null)}
        size="lg"
        title={detail?.roleTitle}
        subtitle={detail ? `${detail.ref} · ${fmtRange(detail.dateStart, detail.dateEnd)} · ${detail.timeSlot}` : ''}
        footer={detail ? (
          <div className="flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500">
            <span>Booked {relativeTime(detail.createdAt)}</span>
            <Badge tone="teal">{statusMeta[detail.status]?.label || detail.status}</Badge>
          </div>
        ) : undefined}
      >
        {detail && <BookingDetail booking={detail} viewerId={user?.id || ''} onCopy={copyShiftDetails} />}
      </Modal>

      {/* rating modal */}
      <Modal
        open={!!ratingFor}
        onClose={() => setRatingFor(null)}
        size="sm"
        title="Rate this professional"
        subtitle={ratingFor ? `${ratingFor.counterpart?.name} · ${ratingFor.ref}` : ''}
        footer={
          <Button
            className="w-full"
            onClick={async () => {
              if (!ratingFor) return;
              await act(ratingFor, 'rate', 'Thanks — your review was saved.', { stars, review });
              setRatingFor(null);
            }}
            icon={<Star className="h-4 w-4" />}
          >
            Submit {stars}-star review
          </Button>
        }
      >
        <div className="space-y-4">
          <div className="flex justify-center gap-2">
            {[1, 2, 3, 4, 5].map((n) => (
              <button key={n} onClick={() => setStars(n)} className="transition hover:scale-110" aria-label={`${n} stars`}>
                <Star className={cn('h-8 w-8', n <= stars ? 'fill-amber-400 text-amber-400' : 'text-slate-300')} />
              </button>
            ))}
          </div>
          <Textarea placeholder="How was the shift? Punctuality, notes, patient manner…" value={review} onChange={(e) => setReview(e.target.value)} />
          <p className="text-[10px] text-slate-400">Reviews are public on the professional&apos;s profile. Keep it factual — no patient information.</p>
        </div>
      </Modal>
    </div>
  );
}

function BookingDetail({ booking: b, viewerId, onCopy }: { booking: Booking; viewerId: string; onCopy: (b: Booking) => void }) {
  const isHirer = b.hirerId === viewerId;
  const addressVisible = ['accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'released', 'closed', 'disputed'].includes(b.status);
  const stepIndex = ['requested', 'pending_response', 'accepted', 'in_progress', 'completed_by_pro', 'pending_release', 'released', 'closed'].indexOf(b.status);
  const progress = Math.max(8, Math.round(((stepIndex + 1) / 8) * 100));

  return (
    <div className="space-y-4">
      <div className="rounded-2xl border border-teal-900/10 bg-white p-4">
        <div className="flex items-center justify-between">
          <span className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">Escrow status</span>
          <div className="flex items-center gap-2">
            <Button size="xs" variant="ghost" onClick={() => onCopy(b)} icon={<Copy className="h-3 w-3" />}>Copy details</Button>
            <Badge tone={b.escrowCents ? 'teal' : 'neutral'}>{b.escrowCents ? `${zar(b.escrowCents)} held` : 'Nothing held'}</Badge>
          </div>
        </div>
        <Progress value={progress} className="mt-2" />
        <div className="mt-2 flex justify-between text-[9px] text-slate-400">
          <span>Requested</span><span>Confirmed</span><span>Complete</span><span>Paid out</span>
        </div>
        <div className="mt-3 grid grid-cols-3 gap-2 text-center">
          {[
            { k: 'Professional pay', v: zar(b.rateCents * b.days) },
            { k: 'Service fee', v: zar(b.serviceFeeCents) },
            { k: 'Total', v: zar(b.totalCents) },
          ].map((x) => (
            <div key={x.k} className="rounded-xl bg-mint-50 px-2 py-2">
              <div className="text-[9px] font-semibold uppercase tracking-wider text-teal-700/60">{x.k}</div>
              <div className="mt-0.5 text-[12px] font-extrabold text-ink-950">{x.v}</div>
            </div>
          ))}
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <div className="rounded-2xl border border-teal-900/10 bg-white p-4">
          <div className="mb-2 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">{isHirer ? 'Professional' : 'Practice'}</div>
          <div className="flex items-center gap-2.5">
            <Avatar name={b.counterpart?.name || 'MediGig'} size="md" />
            <div className="min-w-0">
              <div className="flex items-center gap-1 text-[12px] font-bold text-ink-950">{b.counterpart?.name} {b.counterpart?.verified && <BadgeCheck className="h-3 w-3 text-teal-600" />}</div>
              <div className="text-[10px] text-slate-500">{b.counterpart?.profession}{b.counterpart?.specialty ? ` · ${b.counterpart.specialty}` : ''}</div>
              {b.counterpart?.rating ? <div className="mt-0.5 flex items-center gap-1 text-[9px] text-slate-400"><Stars value={b.counterpart.rating} size={9} /> {b.counterpart.rating} ({b.counterpart.reviewsCount})</div> : null}
            </div>
          </div>
          {addressVisible && b.counterpart?.phone && (
            <a href={`tel:${b.counterpart.phone}`} className="mt-3 flex items-center gap-1.5 rounded-xl bg-mint-50 px-2.5 py-2 text-[11px] font-semibold text-teal-800">
              <Phone className="h-3 w-3" /> {b.counterpart.phone}
            </a>
          )}
        </div>

        <div className="rounded-2xl border border-teal-900/10 bg-white p-4">
          <div className="mb-2 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">Location</div>
          <div className="flex items-start gap-2">
            <span className={cn('mt-0.5 grid h-7 w-7 shrink-0 place-items-center rounded-lg', addressVisible ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-400')}>
              <MapPin className="h-3.5 w-3.5" />
            </span>
            <div>
              <div className="text-[11px] font-semibold leading-snug text-ink-900">{b.location}</div>
              <div className="mt-0.5 text-[9px] text-slate-400">{b.city} · {b.distanceKm} km from you</div>
              {!addressVisible && <div className="mt-1 text-[9px] font-medium text-amber-700">Full address unlocks once the booking is accepted.</div>}
            </div>
          </div>
          {b.notes && <div className="mt-2.5 rounded-xl bg-slate-50 px-2.5 py-2 text-[10px] leading-relaxed text-slate-600">{b.notes}</div>}
        </div>
      </div>

      <div className="rounded-2xl border border-teal-900/10 bg-white p-4">
        <div className="mb-2 flex items-center justify-between">
          <span className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">Activity</span>
          <span className="text-[9px] text-slate-400">{b.timeline?.length || 0} events</span>
        </div>
        <ol className="relative space-y-2.5 border-l border-teal-900/10 pl-4">
          {(b.timeline || []).map((t, i) => (
            <li key={i} className="relative">
              <span className={cn('absolute -left-[21px] top-1 h-2 w-2 rounded-full ring-2 ring-white', i === (b.timeline?.length || 1) - 1 ? 'bg-teal-600' : 'bg-slate-300')} />
              <div className="text-[11px] font-medium text-ink-900">{t.label}</div>
              <div className="text-[9px] text-slate-400">{fmtDate(t.at)} · {fmtTime(t.at)} · {t.by}</div>
            </li>
          ))}
          {!b.timeline?.length && <li className="text-[11px] text-slate-400">No events yet.</li>}
        </ol>
      </div>

      {(b.rating || b.review) && (
        <div className="rounded-2xl border border-amber-200 bg-amber-50/70 p-4">
          <div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[0.14em] text-amber-800"><Star className="h-3 w-3" /> Hirer review</div>
          <div className="mt-1 flex items-center gap-1"><Stars value={b.rating} size={12} /><span className="text-[11px] font-bold text-amber-900">{b.rating}.0</span></div>
          {b.review && <p className="mt-1 text-[11px] leading-relaxed text-amber-900/80">“{b.review}”</p>}
        </div>
      )}
    </div>
  );
}
