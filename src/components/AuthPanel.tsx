import React, { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { ArrowLeft, ArrowRight, BadgeCheck, BriefcaseMedical, Building2, Check, Eye, EyeOff, HeartPulse, Lock, Mail, MapPin, Phone, ShieldCheck, Sparkles, Stethoscope, UserRound } from 'lucide-react';
import { useApp } from '../lib/context';
import { cn, PROFESSIONS, CITIES } from '../lib/utils';
import type { Role } from '../lib/types';
import { Button, Field, Input, Modal, Select } from './ui';

const ROLE_CARDS: { role: Role; title: string; sub: string; icon: React.ReactNode }[] = [
  { role: 'freelancer', title: 'I want to get hired', sub: 'Find freelance medical jobs & shifts', icon: <Stethoscope className="h-5 w-5" /> },
  { role: 'hirer', title: 'I want to hire', sub: 'Hire medical professionals for your clinic', icon: <Building2 className="h-5 w-5" /> },
  { role: 'both', title: 'Both', sub: 'Work and hire on MediGig ZA', icon: <HeartPulse className="h-5 w-5" /> },
];

const DEMOS = [
  { label: 'Hirer', email: 'netcare@medigig.za', icon: <Building2 className="h-3.5 w-3.5" />, tone: 'bg-ink-900 text-white hover:bg-ink-800' },
  { label: 'Professional', email: 'doctor@medigig.za', icon: <Stethoscope className="h-3.5 w-3.5" />, tone: 'bg-mint-100 text-ink-900 hover:bg-mint-200' },
  { label: 'Both', email: 'both@medigig.za', icon: <HeartPulse className="h-3.5 w-3.5" />, tone: 'bg-white text-ink-900 border border-teal-900/10 hover:bg-mint-50' },
];

export function AuthPanel() {
  const { authOpen, closeAuth, authIntent, authRole, login, register, toast } = useApp();
  const [tab, setTab] = useState<'signin' | 'signup'>(authIntent);
  const [role, setRole] = useState<Role>(authRole);
  const [step, setStep] = useState(1);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [form, setForm] = useState({
    name: '', email: '', phone: '', password: '', city: 'Johannesburg',
    profession: 'General Practitioner', specialty: 'General Practice', yearsExperience: '5', dailyRate: '1800', maxDistanceKm: '40',
    practiceName: '', practiceType: 'Private Practice', registrationNumber: '', contactPerson: '',
  });
  const [signin, setSignin] = useState({ email: '', password: '' });

  useEffect(() => {
    if (!authOpen) return;
    setTab(authIntent);
    setRole(authRole);
    setStep(1);
    setError('');
    setBusy(false);
  }, [authOpen, authIntent, authRole]);

  const set = (k: string, v: string) => setForm((f) => ({ ...f, [k]: v }));

  async function submitSignin(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await login(signin.email.trim(), signin.password);
    } catch (err: any) {
      setError(err?.message || 'Could not sign in.');
    } finally {
      setBusy(false);
    }
  }

  async function quickDemo(email: string) {
    setError('');
    setBusy(true);
    try {
      await login(email, 'demo1234');
    } catch (err: any) {
      setError(err?.message || 'Could not sign in.');
    } finally {
      setBusy(false);
    }
  }

  async function submitSignup(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (step === 1) {
      if (form.name.trim().length < 2) return setError('Please enter your full name.');
      if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email)) return setError('Please enter a valid email address.');
      if (form.password.length < 8) return setError('Password must be at least 8 characters.');
      setStep(2);
      return;
    }
    setBusy(true);
    try {
      const payload: Record<string, any> = {
        name: form.name.trim(), email: form.email.trim(), phone: form.phone.trim(), password: form.password,
        role, city: form.city,
      };
      if (role === 'freelancer' || role === 'both') {
        payload.profession = form.profession;
        payload.specialty = form.specialty;
        payload.yearsExperience = Number(form.yearsExperience) || 0;
        payload.dailyRateCents = Math.round(Number(form.dailyRate) * 100) || 150000;
        payload.maxDistanceKm = Number(form.maxDistanceKm) || 40;
      }
      if (role === 'hirer' || role === 'both') {
        payload.practiceName = form.practiceName || `${form.name} Practice`;
        payload.practiceType = form.practiceType;
        payload.registrationNumber = form.registrationNumber;
      }
      await register(payload);
    } catch (err: any) {
      setError(err?.message || 'Could not create your account.');
      if (err?.status === 409) setStep(1);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      open={authOpen}
      onClose={closeAuth}
      size="lg"
      title={tab === 'signin' ? 'Welcome back' : 'Join MediGig ZA'}
      subtitle={tab === 'signin' ? 'Sign in to your wallet, bookings and AI copilot.' : 'Free to join. Escrow only when you book a shift.'}
      className="sm:max-w-4xl"
    >
      <div className="grid gap-6 lg:grid-cols-[1.05fr_.95fr]">
        <div>
          <div className="mb-4 flex rounded-full border border-teal-900/10 bg-white/60 p-1">
            {(['signup', 'signin'] as const).map((t) => (
              <button
                key={t}
                onClick={() => { setTab(t); setError(''); setStep(1); }}
                className={cn('flex-1 rounded-full px-4 py-2 text-[12px] font-bold transition', tab === t ? 'bg-ink-900 text-white shadow' : 'text-ink-700/70 hover:text-ink-900')}
              >
                {t === 'signup' ? 'Create account' : 'Sign in'}
              </button>
            ))}
          </div>

          {error && (
            <div className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>
          )}

          <AnimatePresence mode="wait">
            {tab === 'signin' ? (
              <motion.form key="signin" onSubmit={submitSignin} initial={{ opacity: 0, x: 8 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -8 }} className="space-y-4">
                <Field label="Email">
                  <div className="relative">
                    <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                    <Input className="pl-10" type="email" autoComplete="email" placeholder="you@practice.co.za" value={signin.email} onChange={(e) => setSignin((s) => ({ ...s, email: e.target.value }))} required />
                  </div>
                </Field>
                <Field label="Password">
                  <div className="relative">
                    <Lock className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                    <Input className="pl-10 pr-10" type={showPw ? 'text' : 'password'} autoComplete="current-password" placeholder="••••••••" value={signin.password} onChange={(e) => setSignin((s) => ({ ...s, password: e.target.value }))} required />
                    <button type="button" onClick={() => setShowPw((v) => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-ink-800" aria-label="Toggle password">
                      {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </Field>
                <Button type="submit" className="w-full" loading={busy} icon={<ArrowRight className="h-4 w-4" />}>Sign in</Button>
                <p className="text-center text-[11px] text-slate-500">
                  New here? <button type="button" onClick={() => setTab('signup')} className="font-semibold text-teal-700 hover:underline">Create an account</button>
                </p>
              </motion.form>
            ) : (
              <motion.form key="signup" onSubmit={submitSignup} initial={{ opacity: 0, x: 8 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -8 }} className="space-y-4">
                <div className="flex items-center gap-2">
                  {[1, 2].map((s) => (
                    <div key={s} className="flex flex-1 items-center gap-2">
                      <span className={cn('grid h-6 w-6 place-items-center rounded-full text-[10px] font-bold', step >= s ? 'bg-ink-900 text-white' : 'bg-slate-200 text-slate-500')}>
                        {step > s ? <Check className="h-3 w-3" /> : s}
                      </span>
                      <span className={cn('text-[11px] font-semibold', step >= s ? 'text-ink-900' : 'text-slate-400')}>{s === 1 ? 'Account' : 'Profile'}</span>
                      {s === 1 && <span className="h-px flex-1 bg-slate-200" />}
                    </div>
                  ))}
                </div>

                {step === 1 ? (
                  <>
                    <div className="grid grid-cols-3 gap-2">
                      {ROLE_CARDS.map((r) => (
                        <button
                          key={r.role}
                          type="button"
                          onClick={() => setRole(r.role)}
                          className={cn(
                            'group rounded-xl border p-2.5 text-left transition',
                            role === r.role ? 'border-teal-600 bg-mint-50 shadow-sm ring-2 ring-teal-500/15' : 'border-teal-900/10 bg-white/70 hover:border-teal-900/25',
                          )}
                        >
                          <span className={cn('mb-1.5 grid h-7 w-7 place-items-center rounded-lg', role === r.role ? 'bg-ink-900 text-brand-400' : 'bg-mint-100 text-teal-700')}>{r.icon}</span>
                          <span className="block text-[10px] font-bold leading-tight text-ink-950">{r.title}</span>
                          <span className="mt-0.5 block text-[9px] leading-tight text-slate-500">{r.sub}</span>
                        </button>
                      ))}
                    </div>

                    <div className="grid gap-3 sm:grid-cols-2">
                      <Field label="Full name">
                        <Input placeholder="Dr. Lerato Mokoena" value={form.name} onChange={(e) => set('name', e.target.value)} autoComplete="name" required />
                      </Field>
                      <Field label="Mobile">
                        <Input placeholder="+27 82 555 0198" value={form.phone} onChange={(e) => set('phone', e.target.value)} autoComplete="tel" />
                      </Field>
                    </div>
                    <Field label="Email">
                      <Input type="email" placeholder="you@practice.co.za" value={form.email} onChange={(e) => set('email', e.target.value)} autoComplete="email" required />
                    </Field>
                    <div className="grid gap-3 sm:grid-cols-2">
                      <Field label="Password" hint="At least 8 characters">
                        <Input type={showPw ? 'text' : 'password'} placeholder="••••••••" value={form.password} onChange={(e) => set('password', e.target.value)} autoComplete="new-password" required />
                      </Field>
                      <Field label="City">
                        <Select value={form.city} onChange={(e) => set('city', e.target.value)}>
                          {CITIES.map((c) => <option key={c}>{c}</option>)}
                        </Select>
                      </Field>
                    </div>
                    <Button type="submit" className="w-full" icon={<ArrowRight className="h-4 w-4" />}>Continue</Button>
                    <p className="text-center text-[11px] text-slate-500">
                      Already have an account? <button type="button" onClick={() => setTab('signin')} className="font-semibold text-teal-700 hover:underline">Sign in</button>
                    </p>
                  </>
                ) : (
                  <>
                    <button type="button" onClick={() => setStep(1)} className="inline-flex items-center gap-1 text-[11px] font-semibold text-slate-500 hover:text-ink-900">
                      <ArrowLeft className="h-3 w-3" /> Back to account
                    </button>

                    {(role === 'freelancer' || role === 'both') && (
                      <div className="rounded-2xl border border-teal-900/10 bg-mint-50/60 p-3">
                        <div className="mb-2 flex items-center gap-1.5 text-[11px] font-bold text-ink-900"><Stethoscope className="h-3.5 w-3.5 text-teal-700" /> Professional profile</div>
                        <div className="grid gap-3 sm:grid-cols-2">
                          <Field label="Profession">
                            <Select value={form.profession} onChange={(e) => set('profession', e.target.value)}>
                              {PROFESSIONS.map((p) => <option key={p}>{p}</option>)}
                            </Select>
                          </Field>
                          <Field label="Specialty">
                            <Input placeholder="e.g. ICU, Cardiology" value={form.specialty} onChange={(e) => set('specialty', e.target.value)} />
                          </Field>
                          <Field label="Years experience">
                            <Input type="number" min={0} value={form.yearsExperience} onChange={(e) => set('yearsExperience', e.target.value)} />
                          </Field>
                          <Field label="Daily rate (ZAR)">
                            <Input type="number" min={200} step={50} value={form.dailyRate} onChange={(e) => set('dailyRate', e.target.value)} />
                          </Field>
                          <Field label="Max travel (km)" className="sm:col-span-2">
                            <Input type="number" min={1} value={form.maxDistanceKm} onChange={(e) => set('maxDistanceKm', e.target.value)} />
                          </Field>
                        </div>
                      </div>
                    )}

                    {(role === 'hirer' || role === 'both') && (
                      <div className="rounded-2xl border border-teal-900/10 bg-white p-3">
                        <div className="mb-2 flex items-center gap-1.5 text-[11px] font-bold text-ink-900"><Building2 className="h-3.5 w-3.5 text-teal-700" /> Practice details</div>
                        <div className="grid gap-3 sm:grid-cols-2">
                          <Field label="Practice / hospital">
                            <Input placeholder="Netcare Management" value={form.practiceName} onChange={(e) => set('practiceName', e.target.value)} />
                          </Field>
                          <Field label="Practice type">
                            <Select value={form.practiceType} onChange={(e) => set('practiceType', e.target.value)}>
                              {['Private Practice', 'Private Hospital', 'Public Hospital', 'Clinic', 'Day Hospital', 'Agency'].map((p) => <option key={p}>{p}</option>)}
                            </Select>
                          </Field>
                          <Field label="BHF / registration no." className="sm:col-span-2">
                            <Input placeholder="BHF 0045121" value={form.registrationNumber} onChange={(e) => set('registrationNumber', e.target.value)} />
                          </Field>
                        </div>
                      </div>
                    )}

                    <Button type="submit" className="w-full" loading={busy} icon={<Sparkles className="h-4 w-4" />}>
                      Create my account
                    </Button>
                    <p className="text-center text-[10px] leading-relaxed text-slate-400">
                      By continuing you agree to the MediGig Terms and Privacy Policy. POPIA: we only use your data to match shifts and process payments.
                    </p>
                  </>
                )}
              </motion.form>
            )}
          </AnimatePresence>
        </div>

        {/* right rail */}
        <div className="relative hidden overflow-hidden rounded-2xl bg-ink-950 p-5 text-white lg:block">
          <div className="pointer-events-none absolute -right-12 -top-12 h-48 w-48 rounded-full bg-brand-500/25 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-16 -left-10 h-48 w-48 rounded-full bg-amber-400/10 blur-3xl" />
          <div className="relative">
            <div className="inline-flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.14em] text-mint-100/80">
              <ShieldCheck className="h-3 w-3 text-brand-400" /> Demo workspace
            </div>
            <h3 className="mt-3 text-xl font-extrabold tracking-tight">Skip the form. Look around.</h3>
            <p className="mt-1 text-[11px] leading-relaxed text-mint-100/60">Three seeded accounts — one hirer, one professional, one that does both. Password is <span className="font-mono text-mint-200">demo1234</span>.</p>

            <div className="mt-4 space-y-2">
              {DEMOS.map((d) => (
                <button
                  key={d.email}
                  onClick={() => quickDemo(d.email)}
                  disabled={busy}
                  className={cn('flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-[11px] font-semibold transition disabled:opacity-50', d.tone)}
                >
                  <span className="grid h-6 w-6 place-items-center rounded-lg bg-black/10">{d.icon}</span>
                  <span className="flex-1">
                    {d.label}
                    <span className="block font-mono text-[9px] font-normal opacity-60">{d.email}</span>
                  </span>
                  <ArrowRight className="h-3.5 w-3.5 opacity-60" />
                </button>
              ))}
            </div>

            <div className="mt-5 space-y-2 border-t border-white/10 pt-4">
              {[
                { icon: <WalletIcon />, t: 'Escrow wallet', d: 'Fund bookings up front, release on completion' },
                { icon: <MapPin className="h-3.5 w-3.5" />, t: 'Address privacy', d: 'Exact address unlocks only after acceptance' },
                { icon: <Sparkles className="h-3.5 w-3.5" />, t: 'AI copilot', d: 'Match, draft and explain — in your workspace' },
              ].map((x) => (
                <div key={x.t} className="flex gap-2.5">
                  <span className="mt-0.5 grid h-7 w-7 shrink-0 place-items-center rounded-lg bg-white/8 text-brand-400">{x.icon}</span>
                  <div>
                    <div className="text-[11px] font-bold">{x.t}</div>
                    <div className="text-[10px] leading-snug text-mint-100/50">{x.d}</div>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-5 flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-[10px] text-mint-100/60">
              <BadgeCheck className="h-4 w-4 text-brand-400" /> No card needed until you book.
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
}

function WalletIcon() {
  return <Lock className="h-3.5 w-3.5" />;
}
