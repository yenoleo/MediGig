import React, { useEffect, useMemo, useState } from 'react';
import { CalendarDays, Clock3, MapPin, Sparkles, Wand2 } from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { CITIES, PROFESSIONS, SHIFT_TYPES, TIME_SLOTS, parseRand, todayIso, zar } from '../lib/utils';
import { Button, Field, Input, Modal, Select, Textarea } from './ui';
import { ThinkingState } from './effects';

export function PostShiftModal({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: () => void }) {
  const { me, toast, setAiOpen } = useApp();
  const [form, setForm] = useState({
    title: '',
    profession: 'Registered Nurse',
    specialty: '',
    city: me?.hirer?.city || me?.user?.city || 'Johannesburg',
    dateStart: todayIso(),
    dateEnd: todayIso(),
    timeSlot: '07:00–19:00',
    rate: '950',
    notes: '',
  });
  const [drafting, setDrafting] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setError('');
      setForm((f) => ({
        ...f,
        city: me?.hirer?.city || me?.user?.city || f.city,
        dateStart: todayIso(),
        dateEnd: todayIso(),
      }));
    }
  }, [open, me?.hirer?.city, me?.user?.city]);

  const set = (k: string, v: string) => setForm((f) => ({ ...f, [k]: v }));
  const days = useMemo(() => {
    if (!form.dateStart || !form.dateEnd) return 1;
    return Math.max(1, Math.round((new Date(form.dateEnd).getTime() - new Date(form.dateStart).getTime()) / 86400000) + 1);
  }, [form.dateStart, form.dateEnd]);
  const rateCents = parseRand(form.rate);
  const total = rateCents * days;

  async function draft() {
    setDrafting(true);
    setError('');
    try {
      const { brief } = await api.brief({
        title: form.title || `${form.specialty || form.profession} shift`,
        profession: form.profession,
        specialty: form.specialty,
        dateStart: form.dateStart,
        dateEnd: form.dateEnd,
        timeSlot: form.timeSlot,
        city: form.city,
        rateCents,
        notes: form.notes,
      });
      setForm((f) => ({ ...f, notes: brief, title: f.title || `${f.specialty || f.profession} · ${f.timeSlot.split('–')[0]} start` }));
      toast('Shift brief drafted by the copilot.', 'success');
    } catch (err: any) {
      setError(err?.message || 'Could not draft the brief.');
    } finally {
      setDrafting(false);
    }
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (!form.title.trim()) return setError('Give the shift a short title.');
    if (form.dateEnd < form.dateStart) return setError('End date must be after the start date.');
    setBusy(true);
    try {
      await api.createPost({
        title: form.title.trim(),
        profession: form.profession,
        specialty: form.specialty || SHIFT_TYPES[0],
        city: form.city,
        dateStart: form.dateStart,
        dateEnd: form.dateEnd,
        timeSlot: form.timeSlot,
        rateCents,
        notes: form.notes,
      });
      toast('Shift posted — professionals can now request it.', 'success');
      onCreated();
      setForm((f) => ({ ...f, title: '', notes: '' }));
    } catch (err: any) {
      setError(err?.message || 'Could not post the shift.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      size="lg"
      title="Post a shift"
      subtitle="Open shifts appear to matching professionals. You approve requests — escrow is only funded then."
      footer={
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-[11px] text-slate-500">
            {days} day{days > 1 ? 's' : ''} · <span className="font-bold text-ink-900">{zar(total)}</span> professional pay
            <span className="text-slate-400"> · {zar(Math.round(total * 0.05))} service fee on approval</span>
          </div>
          <div className="flex gap-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>Cancel</Button>
            <Button type="submit" form="post-shift-form" size="sm" loading={busy} icon={<CalendarDays className="h-3.5 w-3.5" />}>Post shift</Button>
          </div>
        </div>
      }
    >
      <form id="post-shift-form" onSubmit={submit} className="space-y-4">
        {error && <div className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>}

        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="Shift title" className="sm:col-span-2">
            <Input placeholder="ICU night shift · 2 nights" value={form.title} onChange={(e) => set('title', e.target.value)} />
          </Field>
          <Field label="Profession">
            <Select value={form.profession} onChange={(e) => set('profession', e.target.value)}>
              {PROFESSIONS.map((p) => <option key={p}>{p}</option>)}
            </Select>
          </Field>
          <Field label="Specialty / unit">
            <Input placeholder="ICU, General Ward, Theatre…" value={form.specialty} onChange={(e) => set('specialty', e.target.value)} list="shift-types" />
            <datalist id="shift-types">
              {SHIFT_TYPES.map((s) => <option key={s} value={s} />)}
            </datalist>
          </Field>
          <Field label="Start date">
            <Input type="date" min={todayIso()} value={form.dateStart} onChange={(e) => { set('dateStart', e.target.value); if (form.dateEnd < e.target.value) set('dateEnd', e.target.value); }} />
          </Field>
          <Field label="End date">
            <Input type="date" min={form.dateStart} value={form.dateEnd} onChange={(e) => set('dateEnd', e.target.value)} />
          </Field>
          <Field label="Shift hours">
            <Select value={form.timeSlot} onChange={(e) => set('timeSlot', e.target.value)}>
              {TIME_SLOTS.map((t) => <option key={t}>{t}</option>)}
            </Select>
          </Field>
          <Field label="Day rate (ZAR)" hint="What the professional takes home per day">
            <Input type="number" min={200} step={50} value={form.rate} onChange={(e) => set('rate', e.target.value)} />
          </Field>
          <Field label="City" className="sm:col-span-2">
            <Select value={form.city} onChange={(e) => set('city', e.target.value)}>
              {CITIES.map((c) => <option key={c}>{c}</option>)}
            </Select>
          </Field>
        </div>

        <div className="rounded-2xl border border-teal-900/10 bg-mint-50/60 p-3">
          <div className="mb-2 flex items-center justify-between">
            <div className="flex items-center gap-1.5 text-[11px] font-bold text-ink-900"><Sparkles className="h-3.5 w-3.5 text-teal-700" /> Shift brief</div>
            <Button type="button" size="xs" variant="white" onClick={draft} loading={drafting} icon={<Wand2 className="h-3 w-3" />}>
              Draft with AI
            </Button>
          </div>
          {drafting && <ThinkingState active phases={['Reading shift details', 'Writing the brief', 'Checking escrow wording']} className="mb-2" />}
          <Textarea
            placeholder="Requirements, unit, acuity, registration needed…"
            value={form.notes}
            onChange={(e) => set('notes', e.target.value)}
            className="min-h-[110px]"
          />
        </div>

        <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-[10px] text-slate-400">
          <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" /> Full address stays private until you approve a request</span>
          <span className="inline-flex items-center gap-1"><Clock3 className="h-3 w-3" /> Escrow funded on approval</span>
        </div>
      </form>
    </Modal>
  );
}
