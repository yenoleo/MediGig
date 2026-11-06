import React, { useEffect, useMemo, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowLeft, ArrowRight, BadgeCheck, CalendarDays, Check, Clock3, CreditCard, Lock,
  MapPin, ShieldCheck, Sparkles, Wallet,
} from 'lucide-react';
import { api, ApiError } from '../lib/api';
import { useApp } from '../lib/context';
import { SHIFT_TYPES, TIME_SLOTS, addDaysIso, cn, daysBetween, fmtDate, todayIso, zar } from '../lib/utils';
import { Avatar, Badge, Button, Field, Input, Modal, Select, Textarea } from './ui';

export function BookingModal() {
  const { hireTarget, setHireTarget, me, refresh, toast, setView, setFocusBooking, setTopUpOpen } = useApp();
  const open = !!hireTarget;
  const pro = hireTarget;

  const [step, setStep] = useState(1);
  const [start, setStart] = useState(addDaysIso(todayIso(), 1));
  const [end, setEnd] = useState(addDaysIso(todayIso(), 1));
  const [timeSlot, setTimeSlot] = useState(TIME_SLOTS[1]);
  const [shiftType, setShiftType] = useState(SHIFT_TYPES[0]);
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [shortfall, setShortfall] = useState(0);

  useEffect(() => {
    if (!open) return;
    setStep(1);
    setError('');
    setShortfall(0);
    setStart(addDaysIso(todayIso(), 1));
    setEnd(addDaysIso(todayIso(), 1));
    setShiftType(SHIFT_TYPES[0]);
    setNotes('');
    if (pro?.specialty) setShiftType(SHIFT_TYPES.includes(pro.specialty) ? pro.specialty : SHIFT_TYPES[0]);
  }, [open, pro?.userId, pro?.specialty]);

  const days = useMemo(() => Math.max(1, daysBetween(start, end)), [start, end]);
  const subtotal = (pro?.dailyRateCents || 0) * days;
  const fee = Math.round(subtotal * 0.05);
  const total = subtotal + fee;
  const balance = me?.wallet?.balanceCents || 0;
  const canAfford = balance >= total;

  if (!pro) return <Modal open={false} onClose={() => {}}><div /></Modal>;

  async function submit() {
    setError('');
    setBusy(true);
    try {
      const { booking } = await api.createBooking({
        freelancerId: pro!.userId,
        dateStart: start,
        dateEnd: end,
        timeSlot,
        shiftType,
        notes,
      });
      toast(`Booking ${booking.ref} sent to ${pro!.name}.`, 'success');
      await refresh();
      setHireTarget(null);
      setView('bookings');
      setFocusBooking(booking.id);
    } catch (err: any) {
      if (err instanceof ApiError && err.status === 402) {
        setShortfall(err.payload?.shortfallCents || Math.max(0, total - balance));
        setError('');
      } else if (err instanceof ApiError && err.status === 409) {
        setStep(1);
        setError(err.message);
      } else {
        setError(err?.message || 'Could not create the booking.');
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={() => setHireTarget(null)}
      size="lg"
      title="Request a booking"
      subtitle={`${pro.name} · ${pro.profession}${pro.specialty ? ` · ${pro.specialty}` : ''}`}
      footer={
        shortfall > 0 ? (
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <div className="text-[11px] text-amber-800">
              Wallet short by <span className="font-extrabold">{zar(shortfall)}</span>. Top up to hold escrow for this shift.
            </div>
            <div className="flex gap-2">
              <Button size="sm" variant="outline" onClick={() => setHireTarget(null)}>Later</Button>
              <Button size="sm" variant="amber" onClick={() => { setHireTarget(null); setTopUpOpen(true); }} icon={<Wallet className="h-3.5 w-3.5" />}>Top up wallet</Button>
            </div>
          </div>
        ) : (
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-1.5 text-[10px] text-slate-500">
              <ShieldCheck className="h-3.5 w-3.5 text-teal-600" />
              {step === 2 ? `${zar(total)} will be held in escrow until completion.` : 'Escrow is only funded when you confirm.'}
            </div>
            <div className="flex gap-2">
              {step === 2 && <Button size="sm" variant="outline" onClick={() => setStep(1)} icon={<ArrowLeft className="h-3.5 w-3.5" />}>Back</Button>}
              {step === 1 ? (
                <Button size="sm" onClick={() => { setError(''); setStep(2); }} disabled={!start || !end || end < start} icon={<ArrowRight className="h-3.5 w-3.5" />}>Review booking</Button>
              ) : (
                <Button size="sm" loading={busy} onClick={submit} icon={<Lock className="h-3.5 w-3.5" />}>Confirm & hold escrow</Button>
              )}
            </div>
          </div>
        )
      }
    >
      {/* pro header */}
      <div className="mb-4 flex items-center gap-3 rounded-2xl border border-teal-900/10 bg-white/70 p-3">
        <Avatar name={pro.name} size="md" online={pro.available} />
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5 text-[13px] font-bold text-ink-950">
            {pro.name}
            {pro.verified && <BadgeCheck className="h-3.5 w-3.5 text-teal-600" />}
          </div>
          <div className="text-[11px] text-slate-500">{pro.profession} · {pro.yearsExperience} yrs experience</div>
          <div className="mt-0.5 flex items-center gap-2 text-[10px] text-slate-400">
            <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />{pro.distanceKm} km away</span>
            <span className="inline-flex items-center gap-1"><Sparkles className="h-3 w-3" />{pro.completedGigs} gigs</span>
          </div>
        </div>
        <div className="text-right">
          <div className="text-[15px] font-extrabold text-ink-950">{zar(pro.dailyRateCents)}</div>
          <div className="text-[9px] text-slate-400">per day</div>
        </div>
      </div>

      {/* steps */}
      <div className="mb-4 flex items-center gap-2">
        {[1, 2].map((s) => (
          <div key={s} className="flex flex-1 items-center gap-2">
            <span className={cn('grid h-6 w-6 place-items-center rounded-full text-[10px] font-bold', step >= s ? 'bg-ink-900 text-white' : 'bg-slate-200 text-slate-500')}>
              {step > s ? <Check className="h-3 w-3" /> : s}
            </span>
            <span className={cn('text-[11px] font-semibold', step >= s ? 'text-ink-900' : 'text-slate-400')}>{s === 1 ? 'Shift details' : 'Review & escrow'}</span>
            {s === 1 && <span className="h-px flex-1 bg-slate-200" />}
          </div>
        ))}
      </div>

      {error && <div className="mb-3 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>}

      {step === 1 ? (
        <div className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2">
            <Field label="Start date">
              <Input type="date" min={todayIso()} value={start} onChange={(e) => { setStart(e.target.value); if (end < e.target.value) setEnd(e.target.value); }} />
            </Field>
            <Field label="End date" hint="Multi-day bookings are prorated by day rate">
              <Input type="date" min={start} value={end} onChange={(e) => setEnd(e.target.value)} />
            </Field>
            <Field label="Shift hours">
              <Select value={timeSlot} onChange={(e) => setTimeSlot(e.target.value)}>
                {TIME_SLOTS.map((t) => <option key={t}>{t}</option>)}
              </Select>
            </Field>
            <Field label="Unit / shift type">
              <Select value={shiftType} onChange={(e) => setShiftType(e.target.value)}>
                {SHIFT_TYPES.map((t) => <option key={t}>{t}</option>)}
              </Select>
            </Field>
          </div>

          <div className="flex flex-wrap gap-1.5">
            {[0, 1, 2, 3, 7].map((offset) => {
              const iso = addDaysIso(todayIso(), offset);
              return (
                <button
                  key={offset}
                  onClick={() => { setStart(iso); setEnd(iso); }}
                  className={cn('rounded-full border px-3 py-1 text-[10px] font-semibold transition', start === iso ? 'border-teal-600 bg-mint-100 text-teal-900' : 'border-teal-900/10 bg-white text-slate-500 hover:border-teal-900/25')}
                >
                  {offset === 0 ? 'Today' : offset === 1 ? 'Tomorrow' : fmtDate(iso, { year: false })}
                </button>
              );
            })}
          </div>

          <Field label="Notes for the professional" hint="Unit, acuity, parking, who to report to — no patient data.">
            <Textarea value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="ICU night cover, 1:2 acuity, report to ward sister at 18:45." className="min-h-[90px]" />
          </Field>

          <div className="rounded-2xl border border-teal-900/10 bg-mint-50/60 p-3">
            <div className="flex items-center justify-between text-[12px]">
              <span className="text-slate-600">{zar(pro.dailyRateCents)} × {days} day{days > 1 ? 's' : ''}</span>
              <span className="font-bold text-ink-950">{zar(subtotal)}</span>
            </div>
            <div className="mt-1 flex items-center justify-between text-[11px] text-slate-500">
              <span>MediGig service fee (5%)</span>
              <span>{zar(fee)}</span>
            </div>
          </div>
        </div>
      ) : (
        <div className="space-y-3">
          <div className="rounded-2xl border border-teal-900/10 bg-white p-4">
            <div className="grid gap-3 sm:grid-cols-2">
              {[
                { k: 'Professional', v: pro.name },
                { k: 'Dates', v: `${fmtDate(start)}${end !== start ? ` → ${fmtDate(end)}` : ''}` },
                { k: 'Hours', v: timeSlot },
                { k: 'Unit', v: shiftType },
                { k: 'Duration', v: `${days} day${days > 1 ? 's' : ''}` },
                { k: 'Distance', v: `${pro.distanceKm} km` },
              ].map((x) => (
                <div key={x.k}>
                  <div className="text-[9px] font-bold uppercase tracking-[0.12em] text-slate-400">{x.k}</div>
                  <div className="mt-0.5 text-[12px] font-semibold text-ink-900">{x.v}</div>
                </div>
              ))}
            </div>
            {notes && <div className="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-[11px] leading-relaxed text-slate-600">{notes}</div>}
          </div>

          <div className="rounded-2xl border border-teal-900/10 bg-ink-950 p-4 text-white">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold uppercase tracking-[0.16em] text-mint-200/60">Escrow summary</span>
              <ShieldCheck className="h-4 w-4 text-brand-400" />
            </div>
            <div className="mt-2 space-y-1.5 text-[11px]">
              <div className="flex justify-between text-mint-100/70"><span>Professional pay ({days}d)</span><span className="font-semibold text-white">{zar(subtotal)}</span></div>
              <div className="flex justify-between text-mint-100/70"><span>Service fee (5%)</span><span className="font-semibold text-white">{zar(fee)}</span></div>
              <div className="mt-1 flex justify-between border-t border-white/10 pt-2 text-[13px]"><span className="font-bold">Held in escrow now</span><span className="font-extrabold">{zar(total)}</span></div>
            </div>
            <div className="mt-3 flex items-center justify-between rounded-xl bg-white/8 px-3 py-2 text-[10px]">
              <span className="inline-flex items-center gap-1 text-mint-100/70"><Wallet className="h-3 w-3" /> Wallet available</span>
              <span className="font-bold">{zar(balance)}</span>
            </div>
            {!canAfford && (
              <div className="mt-2 rounded-lg bg-amber-400/15 px-2.5 py-1.5 text-[10px] font-medium text-amber-200">
                You need {zar(total - balance)} more. You can top up in the next step.
              </div>
            )}
          </div>

          <div className="flex items-start gap-2 rounded-2xl bg-mint-50 px-3 py-2.5 text-[10px] leading-relaxed text-teal-900">
            <Lock className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            Funds are held by MediGig and only released to the professional 5 business days after you confirm the shift is complete. If they decline or you cancel before the shift starts, the full amount returns to your wallet.
          </div>

          <div className="flex items-center gap-1.5 text-[10px] text-slate-400">
            <CreditCard className="h-3 w-3" /> Paying from your MediGig wallet · {me?.paymentMethods?.[0]?.label || 'card on file'}
          </div>
        </div>
      )}
    </Modal>
  );
}
