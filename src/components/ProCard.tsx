import React from 'react';
import { motion } from 'motion/react';
import {
  BadgeCheck, BriefcaseMedical, CalendarCheck2, GraduationCap, MapPin, ShieldCheck,
  Star, Stethoscope, TrendingUp,
} from 'lucide-react';
import type { ProfessionalCard } from '../lib/types';
import { cn, zar } from '../lib/utils';
import { Avatar, Badge, Button, Modal, Stars } from './ui';

export function ProCard({ pro, onView, onHire }: { pro: ProfessionalCard; onView: () => void; onHire: () => void }) {
  return (
    <motion.article
      layout
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="group flex h-full flex-col rounded-2xl border border-teal-900/10 bg-white p-4 transition hover:-translate-y-0.5 hover:border-teal-900/20 hover:shadow-[0_20px_40px_-28px_rgba(4,33,29,.45)]"
    >
      <div className="flex items-start gap-3">
        <Avatar name={pro.name} size="md" online={pro.available} />
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5">
            <button onClick={onView} className="truncate text-[13px] font-bold text-ink-950 hover:text-teal-700">{pro.name}</button>
            {pro.verified && <BadgeCheck className="h-3.5 w-3.5 shrink-0 text-teal-600" />}
          </div>
          <div className="truncate text-[11px] text-slate-500">{pro.profession}{pro.specialty ? ` · ${pro.specialty}` : ''}</div>
          <div className="mt-0.5 flex items-center gap-1 text-[10px] text-slate-400">
            <Stars value={pro.rating} size={10} />
            <span className="font-semibold text-slate-600">{pro.rating ? pro.rating.toFixed(1) : 'New'}</span>
            <span>({pro.reviewsCount})</span>
          </div>
        </div>
        <div className="text-right">
          <div className="text-[13px] font-extrabold text-ink-950">{zar(pro.dailyRateCents)}</div>
          <div className="text-[9px] text-slate-400">/ day</div>
        </div>
      </div>

      {pro.skills.length > 0 && (
        <div className="mt-2.5 flex flex-wrap gap-1">
          {pro.skills.slice(0, 3).map((s) => (
            <span key={s} className="rounded-full bg-mint-50 px-2 py-0.5 text-[9px] font-semibold text-teal-800">{s}</span>
          ))}
          {pro.skills.length > 3 && <span className="rounded-full bg-slate-50 px-2 py-0.5 text-[9px] font-semibold text-slate-400">+{pro.skills.length - 3}</span>}
        </div>
      )}

      <div className="mt-3 flex items-center justify-between gap-2 border-t border-teal-900/6 pt-2.5 text-[10px] text-slate-500">
        <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3 text-teal-600" /> {pro.distanceKm} km · {pro.city}</span>
        <span className={cn('inline-flex items-center gap-1 rounded-full px-2 py-0.5 font-semibold', pro.available ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500')}>
          <span className={cn('h-1.5 w-1.5 rounded-full', pro.available ? 'bg-emerald-500' : 'bg-slate-400')} />
          {pro.available ? 'Available' : 'Booked'}
        </span>
      </div>

      <div className="mt-3 flex gap-2">
        <Button size="xs" variant="outline" className="flex-1" onClick={onView}>Profile</Button>
        <Button size="xs" variant="brand" className="flex-1" onClick={onHire}>Book shift</Button>
      </div>
    </motion.article>
  );
}

export function ProQuickView({ pro, open, onClose, onHire }: { pro: ProfessionalCard | null; open: boolean; onClose: () => void; onHire: () => void }) {
  if (!pro) return null;
  return (
    <Modal
      open={open}
      onClose={onClose}
      size="lg"
      title={
        <span className="flex items-center gap-2">
          {pro.name}
          {pro.verified && <BadgeCheck className="h-4 w-4 text-teal-600" />}
        </span>
      }
      subtitle={`${pro.profession}${pro.specialty ? ` · ${pro.specialty}` : ''} · ${pro.city}`}
      footer={
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="text-[11px] text-slate-500">
            <span className="text-[15px] font-extrabold text-ink-950">{zar(pro.dailyRateCents)}</span> / day
            <span className="ml-2 text-slate-400">· {pro.distanceKm} km away</span>
          </div>
          <Button size="sm" onClick={onHire} icon={<CalendarCheck2 className="h-3.5 w-3.5" />}>Request booking</Button>
        </div>
      }
    >
      <div className="grid gap-5 sm:grid-cols-[1fr_1.1fr]">
        <div>
          <div className="relative overflow-hidden rounded-2xl border border-teal-900/10 bg-gradient-to-br from-mint-100 via-white to-sand-50 p-4">
            <div className="flex items-center gap-3">
              <Avatar name={pro.name} size="xl" />
              <div>
                <div className="flex items-center gap-1.5 text-[15px] font-extrabold text-ink-950">
                  {pro.name}
                  {pro.verified && <ShieldCheck className="h-4 w-4 text-teal-600" />}
                </div>
                <div className="text-[11px] text-slate-500">{pro.profession}</div>
                <div className="mt-1 flex items-center gap-1 text-[10px]">
                  <Stars value={pro.rating} size={11} />
                  <span className="font-bold text-slate-700">{pro.rating ? pro.rating.toFixed(1) : 'New'}</span>
                  <span className="text-slate-400">({pro.reviewsCount} reviews)</span>
                </div>
              </div>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-2">
              {[
                { k: 'Experience', v: `${pro.yearsExperience} yrs` },
                { k: 'Completed', v: `${pro.completedGigs} gigs` },
                { k: 'Rate', v: `${zar(pro.dailyRateCents)}/day` },
                { k: 'Distance', v: `${pro.distanceKm} km` },
              ].map((x) => (
                <div key={x.k} className="rounded-xl border border-teal-900/8 bg-white/80 px-2.5 py-2">
                  <div className="text-[9px] font-bold uppercase tracking-wider text-slate-400">{x.k}</div>
                  <div className="mt-0.5 text-[13px] font-extrabold text-ink-950">{x.v}</div>
                </div>
              ))}
            </div>
            <div className={cn('mt-3 inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[10px] font-bold', pro.available ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500')}>
              <span className={cn('h-1.5 w-1.5 rounded-full', pro.available ? 'bg-emerald-500' : 'bg-slate-400')} />
              {pro.available ? 'Available this week' : 'Fully booked this week'}
            </div>
          </div>

          <div className="mt-4">
            <div className="mb-1.5 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">About</div>
            <p className="text-[12px] leading-relaxed text-slate-600">{pro.bio || 'This professional has not added a bio yet.'}</p>
          </div>
        </div>

        <div className="space-y-4">
          <div>
            <div className="mb-1.5 flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">
              <BriefcaseMedical className="h-3 w-3" /> Skills & units
            </div>
            <div className="flex flex-wrap gap-1.5">
              {(pro.skills.length ? pro.skills : ['General ward', 'Locum cover']).map((s) => (
                <span key={s} className="rounded-full border border-teal-900/10 bg-white px-2.5 py-1 text-[10px] font-semibold text-teal-800">{s}</span>
              ))}
            </div>
          </div>

          <div>
            <div className="mb-1.5 flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">
              <ShieldCheck className="h-3 w-3" /> Certifications
            </div>
            <div className="space-y-1.5">
              {pro.certifications.length === 0 && <div className="rounded-xl border border-dashed border-teal-900/15 p-2.5 text-[11px] text-slate-400">No certifications uploaded yet.</div>}
              {pro.certifications.map((c, i) => (
                <div key={`${c.name}-${i}`} className="flex items-center gap-2 rounded-xl border border-teal-900/8 bg-white px-2.5 py-2">
                  <span className={cn('grid h-6 w-6 place-items-center rounded-lg', c.verified ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700')}>
                    {c.verified ? <BadgeCheck className="h-3.5 w-3.5" /> : <ShieldCheck className="h-3.5 w-3.5" />}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[11px] font-semibold text-ink-900">{c.name}</div>
                    <div className="text-[9px] text-slate-400">{c.issuer}{c.year ? ` · ${c.year}` : ''}</div>
                  </div>
                  <Badge tone={c.verified ? 'emerald' : 'amber'}>{c.verified ? 'Verified' : 'Pending'}</Badge>
                </div>
              ))}
            </div>
          </div>

          <div>
            <div className="mb-1.5 flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.14em] text-slate-400">
              <GraduationCap className="h-3 w-3" /> Education
            </div>
            <div className="space-y-1.5">
              {pro.education.length === 0 && <div className="rounded-xl border border-dashed border-teal-900/15 p-2.5 text-[11px] text-slate-400">No qualifications listed.</div>}
              {pro.education.map((e, i) => (
                <div key={`${e.qualification}-${i}`} className="rounded-xl border border-teal-900/8 bg-white px-2.5 py-2">
                  <div className="text-[11px] font-semibold text-ink-900">{e.qualification}</div>
                  <div className="text-[9px] text-slate-400">{e.institution}{e.year ? ` · ${e.year}` : ''}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="flex items-start gap-2 rounded-xl bg-mint-50 px-3 py-2 text-[10px] leading-relaxed text-teal-800">
            <MapPin className="mt-0.5 h-3 w-3 shrink-0" />
            Only the distance ({pro.distanceKm} km) is shown while browsing. The full address and contact number unlock once the booking is accepted.
          </div>
        </div>
      </div>
    </Modal>
  );
}
