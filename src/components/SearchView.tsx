import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { motion } from 'motion/react';
import {
  CalendarDays, Filter, MapPin, Search, SlidersHorizontal, Sparkles, Stethoscope, X,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { CITIES, PROFESSIONS, cn, fmtDate, parseRand, todayIso, zar } from '../lib/utils';
import type { Post, ProfessionalCard } from '../lib/types';
import { Badge, Button, EmptyState, Field, Input, Segmented, Select, Skeleton, Toggle } from './ui';
import { ProCard, ProQuickView } from './ProCard';
import { Reveal } from './effects';

export function SearchView() {
  const { me, mode, setHireTarget, setAiOpen, toast } = useApp();
  const [tab, setTab] = useState<'pros' | 'shifts'>(mode === 'hire' ? 'pros' : 'pros');
  const [q, setQ] = useState('');
  const [profession, setProfession] = useState('');
  const [city, setCity] = useState(me?.hirer?.city || me?.user?.city || '');
  const [maxRate, setMaxRate] = useState('');
  const [maxDistance, setMaxDistance] = useState('60');
  const [date, setDate] = useState('');
  const [certified, setCertified] = useState(false);
  const [sort, setSort] = useState<'distance' | 'rate' | 'rating'>('distance');
  const [showFilters, setShowFilters] = useState(false);

  const [results, setResults] = useState<ProfessionalCard[]>([]);
  const [total, setTotal] = useState(0);
  const [professions, setProfessions] = useState<string[]>(PROFESSIONS);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<ProfessionalCard | null>(null);

  const [posts, setPosts] = useState<Post[]>([]);
  const [requested, setRequested] = useState<Record<string, boolean>>({});

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.freelancers({
        q: q || undefined,
        profession: profession || undefined,
        city: city || undefined,
        maxRate: maxRate ? String(parseRand(maxRate) / 100) : undefined,
        maxDistance: maxDistance || undefined,
        date: date || undefined,
        certified: certified ? 1 : undefined,
        limit: 40,
      });
      setResults(res.results);
      setTotal(res.total);
      if (res.professions?.length) setProfessions(res.professions);
    } catch (err: any) {
      toast(err?.message || 'Could not load professionals.', 'error');
    } finally {
      setLoading(false);
    }
  }, [q, profession, city, maxRate, maxDistance, date, certified, toast]);

  useEffect(() => {
    const t = setTimeout(load, 220);
    return () => clearTimeout(t);
  }, [load]);

  useEffect(() => {
    api.posts().then((r) => setPosts(r.posts)).catch(() => {});
  }, []);

  const sorted = useMemo(() => {
    const list = [...results];
    if (sort === 'rate') list.sort((a, b) => a.dailyRateCents - b.dailyRateCents);
    else if (sort === 'rating') list.sort((a, b) => b.rating - a.rating);
    else list.sort((a, b) => a.distanceKm - b.distanceKm);
    return list;
  }, [results, sort]);

  const activeFilters = [profession, maxRate, date, certified ? 'certified' : ''].filter(Boolean).length;

  const reset = () => {
    setQ(''); setProfession(''); setMaxRate(''); setDate(''); setCertified(false); setMaxDistance('60');
  };

  async function requestShift(p: Post) {
    try {
      await api.requestPost(p.id);
      setRequested((r) => ({ ...r, [p.id]: true }));
      toast('Request sent — the practice will confirm.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Could not request this shift.', 'error');
    }
  }

  return (
    <div className="space-y-5">
      <Reveal>
        <div className="relative overflow-hidden rounded-[26px] border border-teal-900/10 bg-gradient-to-br from-mint-100 via-white to-sand-50 px-5 py-5 sm:px-7">
          <div className="pointer-events-none absolute -right-20 -top-24 h-64 w-64 rounded-full bg-teal-300/25 blur-3xl" />
          <div className="relative flex flex-wrap items-end justify-between gap-3">
            <div>
              <div className="text-[10px] font-bold uppercase tracking-[0.18em] text-brand-700/70">Discover</div>
              <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-ink-950 sm:text-3xl">Find the right cover, fast.</h1>
              <p className="mt-1 max-w-xl text-[12px] leading-relaxed text-slate-600">
                Search verified professionals by profession, distance and day rate. Addresses stay private until a booking is accepted.
              </p>
            </div>
            <Button size="sm" variant="primary" onClick={() => setAiOpen(true)} icon={<Sparkles className="h-3.5 w-3.5 text-brand-400" />}>
              Ask the copilot
            </Button>
          </div>

          <div className="relative mt-4 flex gap-2">
            <div className="relative flex-1">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <Input
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Search by name, skill or unit — e.g. ICU, theatre, cardiology"
                className="h-12 rounded-full pl-10 pr-10"
              />
              {q && (
                <button onClick={() => setQ('')} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-ink-800" aria-label="Clear search">
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>
            <Button size="md" variant="outline" className="h-12 shrink-0" onClick={() => setShowFilters((v) => !v)} icon={<Filter className="h-4 w-4" />}>
              <span className="hidden sm:inline">Filters</span>
              {activeFilters > 0 && <span className="grid h-4 min-w-4 place-items-center rounded-full bg-teal-700 px-1 text-[9px] font-bold text-white">{activeFilters}</span>}
            </Button>
          </div>
        </div>
      </Reveal>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <Segmented
          value={tab}
          onChange={(v) => setTab(v)}
          options={[
            { value: 'pros', label: `Professionals (${total})`, icon: <Stethoscope className="h-3.5 w-3.5" /> },
            { value: 'shifts', label: `Open shifts (${posts.length})`, icon: <CalendarDays className="h-3.5 w-3.5" /> },
          ]}
        />
        {tab === 'pros' && (
          <div className="flex items-center gap-2">
            <span className="hidden text-[10px] font-semibold uppercase tracking-wider text-slate-400 sm:inline">Sort</span>
            <Select value={sort} onChange={(e) => setSort(e.target.value as any)} className="h-9 w-auto text-[11px]">
              <option value="distance">Closest first</option>
              <option value="rate">Lowest day rate</option>
              <option value="rating">Highest rated</option>
            </Select>
          </div>
        )}
      </div>

      {showFilters && (
        <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} className="overflow-hidden">
          <div className="card p-4">
            <div className="mb-3 flex items-center justify-between">
              <div className="flex items-center gap-1.5 text-[11px] font-bold text-ink-900"><SlidersHorizontal className="h-3.5 w-3.5 text-teal-700" /> Filters</div>
              <button onClick={reset} className="text-[11px] font-semibold text-slate-500 hover:text-ink-900">Reset all</button>
            </div>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <Field label="Profession">
                <Select value={profession} onChange={(e) => setProfession(e.target.value)}>
                  <option value="">Any profession</option>
                  {professions.map((p) => <option key={p} value={p}>{p}</option>)}
                </Select>
              </Field>
              <Field label="City">
                <Select value={city} onChange={(e) => setCity(e.target.value)}>
                  <option value="">Any city</option>
                  {CITIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </Select>
              </Field>
              <Field label="Max day rate (ZAR)">
                <Input type="number" min={0} step={100} placeholder="e.g. 1800" value={maxRate} onChange={(e) => setMaxRate(e.target.value)} />
              </Field>
              <Field label="Available on">
                <Input type="date" min={todayIso()} value={date} onChange={(e) => setDate(e.target.value)} />
              </Field>
            </div>
            <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
              <div className="flex min-w-[220px] flex-1 items-center gap-3">
                <span className="whitespace-nowrap text-[11px] font-semibold text-ink-800">Within {maxDistance} km</span>
                <input type="range" min={5} max={200} step={5} value={maxDistance} onChange={(e) => setMaxDistance(e.target.value)} className="w-full max-w-xs" />
              </div>
              <Toggle checked={certified} onChange={setCertified} label="Verified certifications only" />
            </div>
          </div>
        </motion.div>
      )}

      {tab === 'pros' ? (
        loading ? (
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-[178px] w-full rounded-2xl" />)}
          </div>
        ) : sorted.length === 0 ? (
          <EmptyState
            icon={<Search className="h-5 w-5" />}
            title="No professionals match those filters"
            body="Try widening the distance, raising the day rate, or clearing the availability date."
            action={<Button size="sm" variant="outline" onClick={reset}>Reset filters</Button>}
          />
        ) : (
          <>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {sorted.map((p) => (
                <ProCard key={p.userId} pro={p} onView={() => setSelected(p)} onHire={() => setHireTarget(p)} />
              ))}
            </div>
            <div className="flex items-center justify-center gap-2 pt-1 text-[10px] text-slate-400">
              <MapPin className="h-3 w-3" /> Distances are measured from {city || 'your practice city'} · {total} professional{total === 1 ? '' : 's'} found
            </div>
          </>
        )
      ) : (
        <div className="grid gap-3 lg:grid-cols-2">
          {posts.length === 0 && <EmptyState icon={<CalendarDays className="h-5 w-5" />} title="No open shifts" body="Practices haven't posted any open shifts yet. Check back soon." />}
          {posts.map((p) => {
            const days = Math.max(1, Math.round((new Date(p.dateEnd).getTime() - new Date(p.dateStart).getTime()) / 86400000) + 1);
            const done = requested[p.id];
            return (
              <div key={p.id} className="card flex flex-col gap-3 p-4">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="text-[13px] font-bold text-ink-950">{p.title}</div>
                    <div className="mt-0.5 text-[11px] text-slate-500">{p.profession}{p.specialty ? ` · ${p.specialty}` : ''}</div>
                    <div className="mt-1 text-[10px] text-slate-400">{p.practiceName || 'Private practice'} · {p.city}</div>
                  </div>
                  <div className="shrink-0 text-right">
                    <div className="text-[14px] font-extrabold text-ink-950">{zar(p.rateCents)}</div>
                    <div className="text-[9px] text-slate-400">/ day · {days}d</div>
                  </div>
                </div>
                {p.notes && <p className="line-clamp-2 text-[11px] leading-relaxed text-slate-500">{p.notes}</p>}
                <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-[10px] text-slate-400">
                  <span className="inline-flex items-center gap-1"><CalendarDays className="h-3 w-3" />{fmtDate(p.dateStart, { year: false })}{p.dateEnd !== p.dateStart ? ` → ${fmtDate(p.dateEnd, { year: false })}` : ''}</span>
                  <span>{p.timeSlot}</span>
                  <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />{p.city}</span>
                </div>
                <div className="flex items-center justify-between border-t border-teal-900/6 pt-2.5">
                  <Badge tone="teal">{zar(p.rateCents * days)} total</Badge>
                  <Button size="xs" variant={done ? 'soft' : 'brand'} disabled={done} onClick={() => requestShift(p)}>
                    {done ? 'Requested' : 'Request shift'}
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <ProQuickView
        pro={selected}
        open={!!selected}
        onClose={() => setSelected(null)}
        onHire={() => { const p = selected; setSelected(null); if (p) setHireTarget(p); }}
      />
    </div>
  );
}
