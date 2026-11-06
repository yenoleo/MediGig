import React, { useEffect, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowRight, BadgeCheck, Banknote, CalendarDays, Check, ChevronRight, Clock3,
  HeartPulse, MapPin, Search, ShieldCheck, Sparkles, Star, Stethoscope, Users, Wallet, Zap,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { cn, initialsOf, zar, zarShort } from '../lib/utils';
import type { ProfessionalCard } from '../lib/types';
import { Avatar, Badge, Button, Logo, Stars } from './ui';
import { GradientText, GridBackground, Marquee, NumberTicker, Reveal, ThinkingState, TiltCard, ToolCallRow } from './effects';

const HERO_REQUEST = [
  { name: 'Thandi M.', role: 'Registered Nurse', rating: '5.0', reviews: 12, time: '12 Mar · 09:00–17:00', unit: 'General Ward', initials: 'TM' },
  { name: 'Sipho K.', role: 'Enrolled Nurse', rating: '4.8', reviews: 8, time: '12 Mar · 14:00–22:00', unit: 'Night Shift · ICU', initials: 'SK' },
];

export function Landing() {
  const { openAuth, signedIn, setAiOpen, toast } = useApp();
  const [stats, setStats] = useState<any>(null);
  const [pros, setPros] = useState<ProfessionalCard[]>([]);

  useEffect(() => {
    api.stats().then(setStats).catch(() => {});
    api.freelancers({ limit: 6, city: 'Johannesburg' }).then((r) => setPros(r.results)).catch(() => {});
  }, []);

  const start = (role: 'hirer' | 'freelancer') => openAuth({ role, intent: 'signup' });

  return (
    <div className="relative min-h-screen overflow-x-hidden bg-[#f4faf7]">
      {/* nav */}
      <header className="sticky top-0 z-40 border-b border-teal-900/6 glass">
        <div className="wrap flex h-16 items-center justify-between gap-4">
          <Logo />
          <nav className="hidden items-center gap-7 text-[13px] font-semibold text-ink-800/75 lg:flex">
            <a href="#how" className="transition hover:text-brand-700">How it works</a>
            <a href="#professionals" className="transition hover:text-brand-700">For professionals</a>
            <a href="#practices" className="transition hover:text-brand-700">For practices</a>
            <a href="#copilot" className="group inline-flex items-center gap-1.5 transition hover:text-brand-700">
              AI Copilot <span className="rounded-full bg-mint-100 px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider text-teal-700">new</span>
            </a>
          </nav>
          <div className="flex items-center gap-2">
            <Button size="sm" variant="ghost" onClick={() => openAuth({ intent: 'signin' })}>Sign in</Button>
            <Button size="sm" onClick={() => start('hirer')} icon={<ArrowRight className="h-4 w-4" />}>Get started</Button>
          </div>
        </div>
      </header>

      {/* hero */}
      <section className="relative overflow-hidden">
        <GridBackground />
        <div className="wrap relative grid gap-12 pb-16 pt-12 lg:grid-cols-[1.05fr_.95fr] lg:items-center lg:gap-8 lg:pb-24 lg:pt-20">
          <div>
            <Reveal>
              <div className="inline-flex items-center gap-2 rounded-full border border-teal-900/10 bg-white/70 py-1 pl-1 pr-3 text-[11px] font-semibold text-ink-800 shadow-sm">
                <span className="rounded-full bg-ink-900 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.16em] text-mint-100">MediGig ZA</span>
                Medical freelance marketplace · escrow protected
              </div>
            </Reveal>
            <Reveal delay={0.05}>
              <h1 className="mt-5 text-[42px] font-extrabold leading-[0.98] tracking-[-0.045em] text-ink-950 sm:text-[56px] lg:text-[64px]">
                Locum cover,
                <br />
                <GradientText>without the phone calls.</GradientText>
              </h1>
            </Reveal>
            <Reveal delay={0.1}>
              <p className="mt-5 max-w-xl text-[15px] leading-relaxed text-slate-600 sm:text-base">
                South Africa&apos;s medical freelance marketplace. Practices book verified doctors, nurses and allied
                professionals by the day. Professionals pick up shifts, set their own rate, and get paid through escrow.
              </p>
            </Reveal>
            <Reveal delay={0.15}>
              <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                <Button size="lg" onClick={() => start('hirer')} icon={<Search className="h-4 w-4" />}>Find cover now</Button>
                <Button size="lg" variant="outline" onClick={() => start('freelancer')} icon={<Stethoscope className="h-4 w-4" />}>Pick up shifts</Button>
              </div>
            </Reveal>
            <Reveal delay={0.2}>
              <div className="mt-7 flex flex-wrap items-center gap-x-5 gap-y-2 text-[12px] font-medium text-slate-500">
                <span className="inline-flex items-center gap-1.5"><BadgeCheck className="h-4 w-4 text-teal-600" />HPCSA / SANC verified</span>
                <span className="inline-flex items-center gap-1.5"><ShieldCheck className="h-4 w-4 text-teal-600" />Funds held in escrow</span>
                <span className="inline-flex items-center gap-1.5"><Banknote className="h-4 w-4 text-teal-600" />5-day payouts</span>
              </div>
            </Reveal>
            <Reveal delay={0.25}>
              <div className="mt-8 flex items-center gap-4 border-t border-teal-900/8 pt-5">
                <div className="flex -space-x-2">
                  {pros.slice(0, 4).map((p) => (
                    <Avatar key={p.userId} name={p.name} size="sm" className="ring-2 ring-[#f4faf7]" />
                  ))}
                  {!pros.length && [1, 2, 3, 4].map((i) => <div key={i} className="h-9 w-9 rounded-full bg-mint-200/80 ring-2 ring-[#f4faf7]" />)}
                </div>
                <div className="text-xs leading-tight text-slate-500">
                  <span className="font-bold text-ink-900">2,400+ verified professionals</span>
                  <br />across 9 provinces, from GP locums to ICU nurses
                </div>
              </div>
            </Reveal>
          </div>

          {/* product composition */}
          <Reveal delay={0.15} className="relative">
            <div className="relative mx-auto max-w-[520px] lg:mr-0">
              <div className="absolute -right-6 -top-6 hidden h-24 w-24 rounded-full bg-amber-300/40 blur-2xl sm:block" />
              <TiltCard className="relative">
                <div className="card overflow-hidden p-0">
                  <div className="flex items-center justify-between border-b border-teal-900/8 bg-white/70 px-4 py-3">
                    <div className="flex items-center gap-2">
                      <span className="grid h-7 w-7 place-items-center rounded-lg bg-ink-900 text-white"><HeartPulse className="h-4 w-4" /></span>
                      <div>
                        <div className="text-[12px] font-bold text-ink-950">Netcare Management</div>
                        <div className="text-[10px] text-slate-500">Johannesburg, ZA</div>
                      </div>
                    </div>
                    <Badge tone="teal"><span className="h-1.5 w-1.5 rounded-full bg-teal-500" /> Live</Badge>
                  </div>

                  <div className="grid grid-cols-3 gap-2 bg-[#f7fcfa] p-3">
                    {[
                      { k: 'Active gigs', v: '2 shifts', s: 'today' },
                      { k: 'Covered', v: '8 hrs', s: 'this week' },
                      { k: 'Filled', v: '67%', s: 'fill rate' },
                    ].map((x) => (
                      <div key={x.k} className="rounded-xl border border-teal-900/8 bg-white px-2.5 py-2.5">
                        <div className="text-[9px] font-semibold uppercase tracking-wider text-slate-400">{x.k}</div>
                        <div className="mt-1 text-[15px] font-extrabold text-ink-950">{x.v}</div>
                        <div className="text-[9px] text-slate-400">{x.s}</div>
                      </div>
                    ))}
                  </div>

                  <div className="px-3 pb-3">
                    <div className="mb-2 flex items-center justify-between px-1 pt-1">
                      <span className="text-[11px] font-bold text-ink-900">Requests from freelancers</span>
                      <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[9px] font-bold text-amber-800">3 new</span>
                    </div>
                    <div className="space-y-2">
                      {HERO_REQUEST.map((r, i) => (
                        <motion.div
                          key={r.name}
                          initial={{ opacity: 0, x: 10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: 0.5 + i * 0.15 }}
                          className="flex items-center gap-2.5 rounded-xl border border-teal-900/8 bg-white p-2.5"
                        >
                          <Avatar name={r.name} size="sm" />
                          <div className="min-w-0 flex-1">
                            <div className="flex items-center gap-1 text-[11px] font-bold text-ink-950">{r.name} <Stars value={5} size={9} /></div>
                            <div className="truncate text-[10px] text-slate-500">{r.role} · {r.unit}</div>
                            <div className="text-[9px] text-slate-400">{r.time}</div>
                          </div>
                          <div className="flex gap-1">
                            <span className="grid h-6 w-6 place-items-center rounded-full bg-emerald-50 text-emerald-600"><Check className="h-3 w-3" /></span>
                            <span className="grid h-6 w-6 place-items-center rounded-full bg-slate-100 text-slate-400">×</span>
                          </div>
                        </motion.div>
                      ))}
                    </div>
                  </div>
                </div>
              </TiltCard>

              <motion.div
                initial={{ opacity: 0, y: 14, rotate: -2 }}
                animate={{ opacity: 1, y: 0, rotate: -2 }}
                transition={{ delay: 0.7 }}
                className="absolute -bottom-10 -left-3 w-[210px] rounded-2xl border border-teal-900/10 bg-ink-900 p-4 text-white shadow-2xl sm:-left-8"
              >
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-semibold uppercase tracking-[0.14em] text-mint-200/70">Wallet · escrow</span>
                  <Wallet className="h-3.5 w-3.5 text-brand-400" />
                </div>
                <div className="mt-2 text-[24px] font-extrabold tracking-tight">
                  <NumberTicker value={12500} format={(n) => `R${Math.round(n).toLocaleString('en-ZA')}`} />
                </div>
                <div className="mt-1 flex items-center gap-1 text-[10px] text-mint-200/70"><ShieldCheck className="h-3 w-3" /> R3,600 pending release</div>
                <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-white/10">
                  <motion.div initial={{ width: 0 }} animate={{ width: '68%' }} transition={{ delay: 1, duration: 1 }} className="h-full rounded-full bg-gradient-to-r from-brand-400 to-amber-300" />
                </div>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.95, type: 'spring', stiffness: 200 }}
                className="absolute -right-2 -top-4 flex items-center gap-2 rounded-full border border-teal-900/10 bg-white px-3 py-1.5 text-[10px] font-bold text-ink-900 shadow-lg sm:-right-6"
              >
                <span className="grid h-5 w-5 place-items-center rounded-full bg-mint-100 text-teal-700"><Sparkles className="h-3 w-3" /></span>
                AI matched in 4.2s
              </motion.div>
            </div>
          </Reveal>
        </div>
      </section>

      {/* stats band */}
      <section className="border-y border-teal-900/8 bg-white/60">
        <div className="wrap grid grid-cols-2 gap-6 py-7 sm:grid-cols-4">
          {[
            { label: 'Verified professionals', value: stats?.professionals ?? 10, suffix: '+', icon: <Users className="h-4 w-4" /> },
            { label: 'Practices hiring', value: stats?.practices ?? 2, suffix: '+', icon: <Stethoscope className="h-4 w-4" /> },
            { label: 'Gigs completed', value: stats?.gigs ?? 128, suffix: '', icon: <CalendarDays className="h-4 w-4" /> },
            { label: 'Paid through escrow', value: (stats?.paidOutCents ?? 4820000) / 100, prefix: 'R', format: true, icon: <ShieldCheck className="h-4 w-4" /> },
          ].map((s, i) => (
            <div key={s.label} className="flex items-start gap-3">
              <span className="mt-0.5 grid h-9 w-9 place-items-center rounded-full bg-mint-100 text-teal-700">{s.icon}</span>
              <div>
                <div className="text-[22px] font-extrabold leading-none tracking-tight text-ink-950">
                  {s.prefix}
                  <NumberTicker value={s.value} format={(n) => s.format ? `${Math.round(n / 1000)}k` : `${Math.round(n)}${s.suffix || ''}`} />
                </div>
                <div className="mt-1 text-[11px] font-medium text-slate-500">{s.label}</div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* two sides */}
      <section id="practices" className="wrap py-16 lg:py-24">
        <Reveal>
          <div className="mx-auto max-w-2xl text-center">
            <div className="text-[11px] font-bold uppercase tracking-[0.2em] text-brand-700/70">Two sides, one marketplace</div>
            <h2 className="mt-2 text-3xl font-extrabold tracking-tight text-ink-950 sm:text-4xl">Built for the way healthcare actually staffs.</h2>
            <p className="mt-3 text-sm leading-relaxed text-slate-600">Whether you need a nurse for tonight&apos;s ICU shift or you want to fill your calendar with locum days, MediGig keeps the boring parts — screening, rates, payments — out of the way.</p>
          </div>
        </Reveal>

        <div className="mt-10 grid gap-5 lg:grid-cols-3">
          {[
            {
              tone: 'dark' as const,
              icon: <Stethoscope className="h-5 w-5" />,
              eyebrow: 'For practices & hospitals',
              title: 'Find cover in hours, not days.',
              points: ['Search verified professionals by profession, specialty and km', 'See day rates and availability before you call', 'Fund escrow from your wallet — no invoices chasing'],
              cta: 'Find cover',
              role: 'hirer' as const,
            },
            {
              tone: 'mint' as const,
              icon: <CalendarDays className="h-5 w-5" />,
              eyebrow: 'For professionals',
              title: 'Your rate. Your calendar. Your money.',
              points: ['Set your own day rate and max travel distance', 'Only get requests for dates you have marked available', 'Escrow payouts land 5 business days after completion'],
              cta: 'Pick up shifts',
              role: 'freelancer' as const,
            },
            {
              tone: 'white' as const,
              icon: <Zap className="h-5 w-5" />,
              eyebrow: 'A little AI, where it helps',
              title: 'A copilot that knows your roster.',
              points: ['Matches professionals by distance, rate and certification', 'Drafts shift briefs you can post in one tap', 'Explains escrow and payouts in plain language'],
              cta: 'Try the copilot',
              role: 'hirer' as const,
              ai: true,
            },
          ].map((card, i) => (
            <Reveal key={card.title} delay={i * 0.06}>
              <div className={cn(
                'group relative flex h-full flex-col overflow-hidden rounded-[26px] border p-6 transition-transform duration-300 hover:-translate-y-1',
                card.tone === 'dark' ? 'border-ink-800 bg-ink-900 text-white' : card.tone === 'mint' ? 'border-mint-200 bg-mint-100/70 text-ink-950' : 'border-teal-900/10 bg-white text-ink-950',
              )}>
                {card.tone === 'dark' && <div className="pointer-events-none absolute -right-16 -top-16 h-48 w-48 rounded-full bg-brand-500/20 blur-3xl" />}
                <div className={cn('mb-5 grid h-11 w-11 place-items-center rounded-2xl', card.tone === 'dark' ? 'bg-white/10 text-brand-400' : 'bg-white text-teal-700 shadow-sm')}>{card.icon}</div>
                <div className={cn('text-[10px] font-bold uppercase tracking-[0.18em]', card.tone === 'dark' ? 'text-mint-200/60' : 'text-teal-700/60')}>{card.eyebrow}</div>
                <h3 className="mt-2 text-xl font-extrabold tracking-tight">{card.title}</h3>
                <ul className="mt-4 flex-1 space-y-2.5">
                  {card.points.map((p) => (
                    <li key={p} className="flex gap-2 text-[13px] leading-snug">
                      <span className={cn('mt-0.5 grid h-4 w-4 shrink-0 place-items-center rounded-full', card.tone === 'dark' ? 'bg-brand-500/20 text-brand-400' : 'bg-white text-teal-700')}><Check className="h-2.5 w-2.5" /></span>
                      <span className={card.tone === 'dark' ? 'text-mint-100/80' : 'text-slate-600'}>{p}</span>
                    </li>
                  ))}
                </ul>
                <Button
                  className="mt-6 w-full"
                  variant={card.tone === 'dark' ? 'white' : card.tone === 'mint' ? 'primary' : 'outline'}
                  onClick={() => card.ai ? (signedIn ? (setAiOpen(true)) : openAuth({ intent: 'signin' })) : start(card.role)}
                  icon={<ArrowRight className="h-4 w-4" />}
                >
                  {card.cta}
                </Button>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* professionals */}
      <section id="professionals" className="border-y border-teal-900/8 bg-white/55 py-16 lg:py-24">
        <div className="wrap">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <Reveal>
              <div>
                <div className="text-[11px] font-bold uppercase tracking-[0.2em] text-brand-700/70">Available now</div>
                <h2 className="mt-2 text-3xl font-extrabold tracking-tight text-ink-950 sm:text-4xl">Real people. Real certifications.</h2>
                <p className="mt-2 max-w-xl text-sm text-slate-600">Every professional uploads their HPCSA or SANC registration. Browse by km, rate and availability — full addresses stay private until a booking is accepted.</p>
              </div>
            </Reveal>
            <Reveal delay={0.08}>
              <Button variant="outline" onClick={() => openAuth({ role: 'hirer', intent: 'signup' })} icon={<ArrowRight className="h-4 w-4" />}>Browse all professionals</Button>
            </Reveal>
          </div>

          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {(pros.length ? pros : PLACEHOLDER_PROS).slice(0, 6).map((p, i) => (
              <Reveal key={p.userId} delay={i * 0.04}>
                <button
                  onClick={() => openAuth({ role: 'hirer', intent: 'signup' })}
                  className="group w-full rounded-2xl border border-teal-900/8 bg-white/85 p-4 text-left transition hover:-translate-y-1 hover:border-teal-900/20 hover:shadow-xl"
                >
                  <div className="flex items-start gap-3">
                    <Avatar name={p.name} size="md" online={p.available} />
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-1.5 text-[13px] font-bold text-ink-950">
                        {p.name} {p.verified && <BadgeCheck className="h-3.5 w-3.5 text-teal-600" />}
                      </div>
                      <div className="text-[11px] text-slate-500">{p.profession} · {p.specialty}</div>
                      <div className="mt-0.5 flex items-center gap-1 text-[10px] text-slate-400"><Stars value={p.rating} size={10} /> <span className="font-semibold text-slate-600">{p.rating}</span> ({p.reviewsCount})</div>
                    </div>
                    <div className="text-right">
                      <div className="text-[13px] font-extrabold text-ink-950">{zar(p.dailyRateCents)}</div>
                      <div className="text-[10px] text-slate-400">/ day</div>
                    </div>
                  </div>
                  <div className="mt-3 flex items-center justify-between border-t border-teal-900/6 pt-3 text-[10px] text-slate-500">
                    <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3 text-teal-600" /> {p.distanceKm} km away</span>
                    <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 font-semibold', p.available ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500')}>
                      <span className={cn('h-1.5 w-1.5 rounded-full', p.available ? 'bg-emerald-500' : 'bg-slate-400')} /> {p.available ? 'Available' : 'Booked'}
                    </span>
                  </div>
                </button>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* how it works */}
      <section id="how" className="wrap py-16 lg:py-24">
        <Reveal>
          <div className="mx-auto max-w-2xl text-center">
            <div className="text-[11px] font-bold uppercase tracking-[0.2em] text-brand-700/70">Escrow, end to end</div>
            <h2 className="mt-2 text-3xl font-extrabold tracking-tight text-ink-950 sm:text-4xl">Money moves only when the shift is done.</h2>
            <p className="mt-3 text-sm leading-relaxed text-slate-600">No chasing invoices. No “I&apos;ll pay you Monday.” The hirer funds the booking up front, MediGig holds it, and the professional is paid after completion.</p>
          </div>
        </Reveal>
        <div className="relative mt-10 grid gap-4 lg:grid-cols-4">
          <div className="absolute left-[8%] right-[8%] top-9 hidden h-px border-t border-dashed border-teal-700/25 lg:block" />
          {[
            { icon: <Search className="h-5 w-5" />, k: '01', t: 'Find & compare', d: 'Search by profession, specialty, km and day rate. Certifications are verified before a profile goes live.' },
            { icon: <Wallet className="h-5 w-5" />, k: '02', t: 'Fund the escrow', d: 'Top up your wallet with PayFast, Paystack, card or EFT. The total is held the moment you confirm.' },
            { icon: <CalendarDays className="h-5 w-5" />, k: '03', t: 'Work the shift', d: 'Acceptance unlocks the full address and contact number. Both sides get reminders and a simple status trail.' },
            { icon: <Banknote className="h-5 w-5" />, k: '04', t: 'Release & rate', d: 'Hirer confirms completion. Escrow releases to the professional 5 business days later, minus nothing.' },
          ].map((s, i) => (
            <Reveal key={s.k} delay={i * 0.07}>
              <div className="relative rounded-2xl border border-teal-900/8 bg-white/80 p-5">
                <div className="flex items-center justify-between">
                  <span className="grid h-12 w-12 place-items-center rounded-2xl bg-ink-900 text-brand-400 shadow-lg">{s.icon}</span>
                  <span className="font-mono text-[11px] font-bold text-teal-700/40">{s.k}</span>
                </div>
                <h3 className="mt-4 text-[15px] font-bold text-ink-950">{s.t}</h3>
                <p className="mt-1.5 text-[12px] leading-relaxed text-slate-500">{s.d}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* copilot */}
      <section id="copilot" className="relative overflow-hidden bg-ink-950 py-16 text-white lg:py-24">
        <div className="pointer-events-none absolute inset-0 opacity-60">
          <div className="absolute -left-20 top-0 h-96 w-96 rounded-full bg-brand-500/20 blur-[120px]" />
          <div className="absolute bottom-0 right-0 h-96 w-96 rounded-full bg-amber-400/10 blur-[120px]" />
        </div>
        <div className="wrap relative grid gap-10 lg:grid-cols-2 lg:items-center">
          <div>
            <Badge tone="dark" className="border-white/15 bg-white/5 text-mint-100"><Sparkles className="h-3 w-3 text-brand-400" /> MediGig Copilot</Badge>
            <h2 className="mt-4 text-3xl font-extrabold tracking-tight sm:text-4xl">Ask for cover like you&apos;d ask a colleague.</h2>
            <p className="mt-3 max-w-lg text-sm leading-relaxed text-mint-100/70">
              The copilot reads only your own wallet, bookings and availability, then searches the marketplace for you.
              It can rank professionals, draft a shift brief, and explain exactly when money moves.
            </p>
            <div className="mt-6 grid gap-3 sm:grid-cols-2">
              {[
                { t: 'Match by intent', d: '“ICU nurse, tomorrow night, under R800”' },
                { t: 'Draft the brief', d: 'A ready-to-post shift description' },
                { t: 'Explain escrow', d: 'Plain-language payment timelines' },
                { t: 'Wallet answers', d: 'What is available vs held' },
              ].map((x) => (
                <div key={x.t} className="rounded-xl border border-white/10 bg-white/5 p-3">
                  <div className="text-[12px] font-bold text-white">{x.t}</div>
                  <div className="mt-0.5 text-[11px] text-mint-100/60">{x.d}</div>
                </div>
              ))}
            </div>
            <Button className="mt-7" variant="white" onClick={() => signedIn ? setAiOpen(true) : openAuth({ intent: 'signup' })} icon={<Sparkles className="h-4 w-4" />}>
              Try the copilot
            </Button>
          </div>

          <div className="relative">
            <div className="rounded-[26px] border border-white/10 bg-white/[0.06] p-3 backdrop-blur">
              <div className="rounded-[20px] bg-[#f8fcfa] p-4 text-ink-950 shadow-2xl">
                <div className="flex items-center gap-2 border-b border-teal-900/8 pb-3">
                  <span className="grid h-8 w-8 place-items-center rounded-xl bg-ink-900 text-brand-400"><Sparkles className="h-4 w-4" /></span>
                  <div>
                    <div className="text-[12px] font-bold">MediGig Copilot</div>
                    <div className="text-[9px] text-slate-400">glm-4.5 · reads only your workspace</div>
                  </div>
                  <Badge tone="teal" className="ml-auto">online</Badge>
                </div>
                <div className="space-y-2.5 pt-3">
                  <div className="ml-auto w-fit max-w-[85%] rounded-2xl rounded-br-md bg-ink-900 px-3 py-2 text-[11px] font-medium text-white">
                    Find me an ICU nurse near Johannesburg for tomorrow night.
                  </div>
                  <ThinkingState active phases={['Searching verified nurses', 'Checking tomorrow night', 'Ranking by distance']} />
                  <ToolCallRow label="search_freelancers" detail="4 matches" index={0} />
                  <ToolCallRow label="check_availability" detail="2 free · 19:00" index={1} />
                  <div className="rounded-2xl rounded-bl-md border border-teal-900/8 bg-white p-3 text-[11px] leading-relaxed text-slate-600">
                    Sipho Khumalo is your closest ICU nurse at <span className="font-bold text-ink-950">R720/day</span>, 13.4 km away. He has an SANC registration on file and is available for night shift. Want me to draft the booking?
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    <span className="rounded-full border border-teal-900/10 bg-white px-2.5 py-1 text-[9px] font-semibold text-teal-800">Review Sipho</span>
                    <span className="rounded-full border border-teal-900/10 bg-white px-2.5 py-1 text-[9px] font-semibold text-teal-800">Draft brief</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* trust */}
      <section className="wrap py-14">
        <div className="grid gap-4 sm:grid-cols-3">
          {[
            { icon: <ShieldCheck className="h-5 w-5" />, t: 'Escrow by design', d: 'Hirer funds are held before a shift starts. Declines and cancellations refund automatically.' },
            { icon: <BadgeCheck className="h-5 w-5" />, t: 'Registration checked', d: 'HPCSA, SANC and BHF numbers are captured with PDF proof. Verified badges are visible on profiles.' },
            { icon: <HeartPulse className="h-5 w-5" />, t: 'POPIA-minded', d: 'Addresses unlock only after acceptance. No patient data ever enters the marketplace.' },
          ].map((x) => (
            <div key={x.t} className="flex gap-3 rounded-2xl border border-teal-900/8 bg-white/70 p-4">
              <span className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-mint-100 text-teal-700">{x.icon}</span>
              <div>
                <div className="text-[13px] font-bold text-ink-950">{x.t}</div>
                <div className="mt-0.5 text-[11px] leading-relaxed text-slate-500">{x.d}</div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* closing */}
      <section className="wrap pb-16">
        <div className="relative overflow-hidden rounded-[30px] border border-teal-900/10 bg-gradient-to-br from-mint-100 via-white to-sand-50 px-6 py-10 text-center sm:px-12 lg:py-14">
          <div className="pointer-events-none absolute inset-0 opacity-60"><GridBackground className="opacity-40" /></div>
          <div className="relative">
            <h2 className="mx-auto max-w-2xl text-3xl font-extrabold tracking-tight text-ink-950 sm:text-4xl">Cover the next shift before the group chat wakes up.</h2>
            <p className="mx-auto mt-3 max-w-lg text-sm text-slate-600">Join the practices and professionals using MediGig ZA. Free to join, free to browse, escrow only when you book.</p>
            <div className="mt-7 flex flex-col justify-center gap-3 sm:flex-row">
              <Button size="lg" onClick={() => start('hirer')} icon={<ArrowRight className="h-4 w-4" />}>Hire a professional</Button>
              <Button size="lg" variant="outline" onClick={() => start('freelancer')} icon={<Stethoscope className="h-4 w-4" />}>Find shifts</Button>
            </div>
            <p className="mt-4 text-[11px] text-slate-400">Demo accounts are available on the sign-in screen.</p>
          </div>
        </div>
      </section>

      <footer className="border-t border-teal-900/8 bg-white/60">
        <div className="wrap flex flex-col gap-6 py-8 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <Logo />
            <p className="mt-2 max-w-sm text-[11px] leading-relaxed text-slate-500">MediGig ZA (Pty) Ltd · South African medical freelance marketplace. This is a product demo — no real payments are processed.</p>
          </div>
          <div className="flex flex-wrap gap-x-6 gap-y-2 text-[11px] font-semibold text-slate-500">
            <a href="#how" className="hover:text-teal-700">How it works</a>
            <a href="#professionals" className="hover:text-teal-700">Professionals</a>
            <a href="#practices" className="hover:text-teal-700">Practices</a>
            <button onClick={() => openAuth({ intent: 'signin' })} className="hover:text-teal-700">Sign in</button>
          </div>
        </div>
      </footer>
    </div>
  );
}

const PLACEHOLDER_PROS: ProfessionalCard[] = [
  { userId: 'p1', name: 'Dr. Michael Johnson', initials: 'MJ', profession: 'General Practitioner', specialty: 'General Practice', city: 'Cape Town', distanceKm: 2.1, dailyRateCents: 200000, yearsExperience: 9, rating: 4.9, reviewsCount: 34, completedGigs: 58, verified: true, available: true, skills: [], certifications: [], education: [], bio: '' },
  { userId: 'p2', name: 'Kagiso Ndlovu', initials: 'KN', profession: 'Registered Nurse', specialty: 'Theatre / Recovery', city: 'Cape Town', distanceKm: 4.5, dailyRateCents: 145000, yearsExperience: 5, rating: 4.7, reviewsCount: 6, completedGigs: 18, verified: true, available: true, skills: [], certifications: [], education: [], bio: '' },
  { userId: 'p3', name: 'Thandi Mabaso', initials: 'TM', profession: 'Registered Nurse', specialty: 'General Ward', city: 'Johannesburg', distanceKm: 4.2, dailyRateCents: 95000, yearsExperience: 6, rating: 5, reviewsCount: 12, completedGigs: 41, verified: true, available: true, skills: [], certifications: [], education: [], bio: '' },
  { userId: 'p4', name: 'Dr. Lerato Mokoena', initials: 'LM', profession: 'Specialist', specialty: 'General Cardiology', city: 'Johannesburg', distanceKm: 6.5, dailyRateCents: 180000, yearsExperience: 12, rating: 5, reviewsCount: 12, completedGigs: 34, verified: true, available: true, skills: [], certifications: [], education: [], bio: '' },
  { userId: 'p5', name: 'Lerato Pillay', initials: 'LP', profession: 'Physiotherapist', specialty: 'Rehabilitation', city: 'Sandton', distanceKm: 11.8, dailyRateCents: 110000, yearsExperience: 8, rating: 4.9, reviewsCount: 15, completedGigs: 29, verified: true, available: false, skills: [], certifications: [], education: [], bio: '' },
  { userId: 'p6', name: 'Dr. Anisha Naidoo', initials: 'AN', profession: 'Specialist', specialty: 'Paediatrics', city: 'Durban', distanceKm: 8.3, dailyRateCents: 240000, yearsExperience: 14, rating: 4.8, reviewsCount: 21, completedGigs: 40, verified: true, available: true, skills: [], certifications: [], education: [], bio: '' },
];
