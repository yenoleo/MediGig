import React, { useEffect, useMemo, useRef, useState } from 'react';
import { motion } from 'motion/react';
import {
  Award, BadgeCheck, Building2, CalendarDays, ChevronLeft, ChevronRight, FileText,
  GraduationCap, MapPin, Phone, Plus, Save, ShieldCheck, Stethoscope, Trash2, Upload, UserRound,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { CITIES, PROFESSIONS, cn, monthMatrix, parseRand, todayIso, zar } from '../lib/utils';
import { Avatar, Badge, Button, Field, Input, Progress, SectionTitle, Segmented, Select, Textarea } from './ui';

type Tab = 'personal' | 'professional' | 'practice' | 'verification';

export function ProfileView() {
  const { me, refresh, toast, mode } = useApp();
  const user = me?.user;
  const fl = me?.freelancer;
  const hirer = me?.hirer;
  const [tab, setTab] = useState<Tab>('personal');
  const [busy, setBusy] = useState(false);

  const [personal, setPersonal] = useState({ name: '', phone: '', city: '', initials: '' });
  const [pro, setPro] = useState({
    profession: 'General Practitioner', specialty: '', yearsExperience: '0', dailyRate: '1500',
    maxDistanceKm: '40', bio: '', skills: '',
  });
  const [practice, setPractice] = useState({ practiceName: '', practiceType: 'Private Practice', registrationNumber: '', contactPerson: '', city: '', address: '', staffCount: '0' });
  const [certs, setCerts] = useState<any[]>([]);
  const [education, setEducation] = useState<any[]>([]);
  const [availability, setAvailability] = useState<string[]>([]);
  const [month, setMonth] = useState(() => new Date());
  const [newCert, setNewCert] = useState({ name: '', number: '', issuer: '', year: '' });
  const [newEdu, setNewEdu] = useState({ qualification: '', institution: '', year: '' });
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!user) return;
    setPersonal({ name: user.name, phone: user.phone || '', city: user.city || '', initials: user.initials || '' });
  }, [user?.id]);

  useEffect(() => {
    if (!fl) return;
    setPro({
      profession: fl.profession || 'General Practitioner',
      specialty: fl.specialty || '',
      yearsExperience: String(fl.yearsExperience || 0),
      dailyRate: String(Math.round((fl.dailyRateCents || 150000) / 100)),
      maxDistanceKm: String(fl.maxDistanceKm || 40),
      bio: fl.bio || '',
      skills: (fl.skills || []).join(', '),
    });
    setCerts(fl.certifications || []);
    setEducation(fl.education || []);
    setAvailability(fl.availableDates || []);
  }, [fl?.userId, fl?.dailyRateCents]);

  useEffect(() => {
    if (!hirer) return;
    setPractice({
      practiceName: hirer.practiceName || '',
      practiceType: hirer.practiceType || 'Private Practice',
      registrationNumber: hirer.registrationNumber || '',
      contactPerson: hirer.contactPerson || '',
      city: hirer.city || '',
      address: hirer.address || '',
      staffCount: String(hirer.staffCount || 0),
    });
  }, [hirer?.userId]);

  useEffect(() => {
    if (fl && mode === 'work') setTab('professional');
    else if (hirer && mode === 'hire' && !fl) setTab('practice');
  }, [mode, !!fl, !!hirer]);

  const tabs: { value: Tab; label: string }[] = useMemo(() => [
    { value: 'personal', label: 'Personal' },
    ...(fl ? [{ value: 'professional' as Tab, label: 'Professional' }] : []),
    ...(hirer ? [{ value: 'practice' as Tab, label: 'Practice' }] : []),
    ...(fl || hirer ? [{ value: 'verification' as Tab, label: 'Verification' }] : []),
  ], [fl, hirer]);

  const cells = useMemo(() => monthMatrix(month.getFullYear(), month.getMonth()), [month]);

  if (!user) return null;

  async function savePersonal() {
    setBusy(true);
    try {
      await api.updateProfile({ name: personal.name, phone: personal.phone, city: personal.city, initials: personal.initials });
      await refresh();
      toast('Profile saved.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Could not save your profile.', 'error');
    } finally {
      setBusy(false);
    }
  }

  async function savePro() {
    setBusy(true);
    try {
      await api.updateFreelancer({
        profession: pro.profession,
        specialty: pro.specialty,
        yearsExperience: Number(pro.yearsExperience) || 0,
        dailyRateCents: parseRand(pro.dailyRate),
        maxDistanceKm: Number(pro.maxDistanceKm) || 40,
        bio: pro.bio,
        skills: pro.skills.split(',').map((s) => s.trim()).filter(Boolean),
        certifications: certs,
        education,
        availableDates: availability,
        city: personal.city,
      });
      await refresh();
      toast('Professional profile updated.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Could not update your professional profile.', 'error');
    } finally {
      setBusy(false);
    }
  }

  async function savePractice() {
    setBusy(true);
    try {
      await api.updateHirer({ ...practice, staffCount: Number(practice.staffCount) || 0 });
      await refresh();
      toast('Practice details saved.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Could not save the practice details.', 'error');
    } finally {
      setBusy(false);
    }
  }

  async function onFile(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 6 * 1024 * 1024) return toast('File must be smaller than 6 MB.', 'error');
    setUploading(true);
    try {
      const data = await new Promise<string>((resolve, reject) => {
        const r = new FileReader();
        r.onload = () => resolve(String(r.result || ''));
        r.onerror = reject;
        r.readAsDataURL(file);
      });
      const res = await api.upload({ name: file.name, mime: file.type || 'application/pdf', data });
      const cert = {
        name: newCert.name || file.name.replace(/\.[^.]+$/, ''),
        number: newCert.number,
        issuer: newCert.issuer,
        year: newCert.year,
        url: res.url,
        fileName: res.name,
        verified: false,
      };
      setCerts((c) => [...c, cert]);
      setNewCert({ name: '', number: '', issuer: '', year: '' });
      toast('Document uploaded — pending verification.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Upload failed.', 'error');
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  }

  const verifiedCerts = certs.filter((c) => c.verified).length;
  const profileSteps = [
    { k: 'Personal details', ok: !!user.name && !!user.phone },
    ...(fl ? [
      { k: 'Professional bio', ok: !!pro.bio },
      { k: 'Day rate set', ok: parseRand(pro.dailyRate) > 0 },
      { k: 'Certification uploaded', ok: certs.length > 0 },
      { k: 'Availability marked', ok: availability.length > 0 },
    ] : []),
    ...(hirer ? [
      { k: 'Practice details', ok: !!practice.practiceName },
      { k: 'BHF / registration', ok: !!practice.registrationNumber },
      { k: 'Practice address', ok: !!practice.address },
    ] : []),
  ];
  const pct = Math.round((profileSteps.filter((s) => s.ok).length / Math.max(1, profileSteps.length)) * 100);

  return (
    <div className="space-y-5">
      <div className="relative overflow-hidden rounded-[26px] border border-teal-900/10 bg-gradient-to-br from-mint-100 via-white to-sand-50 p-5 sm:p-6">
        <div className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-teal-300/25 blur-3xl" />
        <div className="relative flex flex-wrap items-center gap-4">
          <Avatar name={user.name} size="xl" ring />
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-extrabold tracking-tight text-ink-950 sm:text-2xl">{user.name}</h1>
              {user.verified ? <Badge tone="emerald"><BadgeCheck className="h-3 w-3" /> Verified</Badge> : <Badge tone="amber">Verification pending</Badge>}
            </div>
            <div className="mt-0.5 text-[12px] text-slate-600">
              {fl?.profession || hirer?.practiceName || 'MediGig member'}{fl?.specialty ? ` · ${fl.specialty}` : ''}
            </div>
            <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-[10px] text-slate-500">
              <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />{user.city}</span>
              <span className="inline-flex items-center gap-1"><Phone className="h-3 w-3" />{user.phone || 'No phone yet'}</span>
              <span>{user.email}</span>
            </div>
          </div>
          <div className="w-full max-w-[190px] rounded-2xl border border-teal-900/10 bg-white/80 p-3">
            <div className="flex items-center justify-between text-[9px] font-bold uppercase tracking-wider text-slate-400"><span>Profile strength</span><span className="text-ink-900">{pct}%</span></div>
            <Progress value={pct} className="mt-2" />
            <div className="mt-1.5 text-[9px] text-slate-400">{profileSteps.filter((s) => s.ok).length}/{profileSteps.length} completed</div>
          </div>
        </div>
      </div>

      <Segmented value={tab} onChange={setTab} options={tabs} />

      {tab === 'personal' && (
        <section className="card p-4 sm:p-5">
          <SectionTitle eyebrow="Account" title="Personal details" sub="Shown to the other side of a booking. No profile photos — initials only, by design." />
          <div className="grid gap-3 sm:grid-cols-2">
            <Field label="Full name"><Input value={personal.name} onChange={(e) => setPersonal({ ...personal, name: e.target.value })} /></Field>
            <Field label="Mobile number"><Input value={personal.phone} onChange={(e) => setPersonal({ ...personal, phone: e.target.value })} placeholder="+27 82 555 0198" /></Field>
            <Field label="City">
              <Select value={personal.city} onChange={(e) => setPersonal({ ...personal, city: e.target.value })}>
                {CITIES.map((c) => <option key={c}>{c}</option>)}
              </Select>
            </Field>
            <Field label="Avatar initials" hint="2–3 letters shown instead of a photo">
              <Input maxLength={3} value={personal.initials} onChange={(e) => setPersonal({ ...personal, initials: e.target.value.toUpperCase() })} />
            </Field>
          </div>
          <div className="mt-4 flex items-center justify-between border-t border-teal-900/6 pt-3">
            <div className="text-[10px] text-slate-400">Email is used for sign-in and cannot be changed in this demo.</div>
            <Button size="sm" loading={busy} onClick={savePersonal} icon={<Save className="h-3.5 w-3.5" />}>Save changes</Button>
          </div>
        </section>
      )}

      {tab === 'professional' && fl && (
        <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
          <section className="card p-4 sm:p-5">
            <SectionTitle eyebrow="Professional" title="Your locum profile" sub="This is what practices see when they search. Rate and distance drive your matches." />
            <div className="grid gap-3 sm:grid-cols-2">
              <Field label="Profession">
                <Select value={pro.profession} onChange={(e) => setPro({ ...pro, profession: e.target.value })}>
                  {PROFESSIONS.map((p) => <option key={p}>{p}</option>)}
                </Select>
              </Field>
              <Field label="Specialty / unit"><Input value={pro.specialty} onChange={(e) => setPro({ ...pro, specialty: e.target.value })} placeholder="ICU, Theatre, Cardiology…" /></Field>
              <Field label="Years experience"><Input type="number" min={0} value={pro.yearsExperience} onChange={(e) => setPro({ ...pro, yearsExperience: e.target.value })} /></Field>
              <Field label="Daily rate (ZAR)" hint={`Currently ${zar(parseRand(pro.dailyRate))} per day`}>
                <Input type="number" min={200} step={50} value={pro.dailyRate} onChange={(e) => setPro({ ...pro, dailyRate: e.target.value })} />
              </Field>
              <Field label="Max travel distance" hint={`${pro.maxDistanceKm} km from ${personal.city || user.city}`} className="sm:col-span-2">
                <input type="range" min={5} max={200} step={5} value={pro.maxDistanceKm} onChange={(e) => setPro({ ...pro, maxDistanceKm: e.target.value })} className="w-full" />
              </Field>
              <Field label="Skills & units" hint="Comma separated" className="sm:col-span-2">
                <Input value={pro.skills} onChange={(e) => setPro({ ...pro, skills: e.target.value })} placeholder="ICU, Ventilator checks, Night shift" />
              </Field>
              <Field label="Bio" className="sm:col-span-2" hint={`${pro.bio.length}/1200 characters`}>
                <Textarea maxLength={1200} value={pro.bio} onChange={(e) => setPro({ ...pro, bio: e.target.value })} placeholder="Registration, experience, the units you're strongest in…" />
              </Field>
            </div>
            <div className="mt-4 flex justify-end border-t border-teal-900/6 pt-3">
              <Button size="sm" loading={busy} onClick={savePro} icon={<Save className="h-3.5 w-3.5" />}>Save professional profile</Button>
            </div>
          </section>

          <div className="space-y-5">
            <section className="card p-4 sm:p-5">
              <SectionTitle eyebrow="Availability" title="Mark your days" sub="Only marked days can be booked." />
              <div className="mb-2 flex items-center justify-between">
                <button onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))} className="grid h-7 w-7 place-items-center rounded-full border border-teal-900/10 bg-white text-ink-700"><ChevronLeft className="h-3.5 w-3.5" /></button>
                <span className="text-[12px] font-bold text-ink-900">{month.toLocaleDateString('en-ZA', { month: 'long', year: 'numeric' })}</span>
                <button onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))} className="grid h-7 w-7 place-items-center rounded-full border border-teal-900/10 bg-white text-ink-700"><ChevronRight className="h-3.5 w-3.5" /></button>
              </div>
              <div className="grid grid-cols-7 gap-1 text-center">
                {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((d, i) => <div key={i} className="text-[9px] font-bold uppercase text-slate-400">{d}</div>)}
                {cells.map((c) => {
                  const on = availability.includes(c.iso);
                  const past = c.iso < todayIso();
                  return (
                    <button
                      key={c.iso}
                      disabled={past}
                      onClick={() => setAvailability((a) => (on ? a.filter((x) => x !== c.iso) : [...a, c.iso].sort()))}
                      className={cn(
                        'relative grid h-8 place-items-center rounded-lg text-[11px] font-semibold transition',
                        !c.current && 'opacity-30',
                        past ? 'cursor-not-allowed text-slate-300' : on ? 'bg-teal-700 text-white shadow-sm' : 'bg-white text-ink-800 hover:bg-mint-100',
                      )}
                    >
                      {c.date.getDate()}
                    </button>
                  );
                })}
              </div>
              <div className="mt-3 flex items-center justify-between rounded-xl bg-mint-50 px-3 py-2 text-[10px] text-teal-800">
                <span><span className="font-bold">{availability.length}</span> days available</span>
                <button onClick={() => setAvailability([])} className="font-semibold hover:underline">Clear</button>
              </div>
              <Button size="sm" className="mt-3 w-full" loading={busy} onClick={savePro} icon={<CalendarDays className="h-3.5 w-3.5" />}>Save availability</Button>
            </section>

            <section className="card p-4 sm:p-5">
              <SectionTitle eyebrow="Education" title="Qualifications" />
              <div className="space-y-2">
                {education.map((e, i) => (
                  <div key={i} className="flex items-start gap-2 rounded-xl border border-teal-900/8 bg-white px-2.5 py-2">
                    <GraduationCap className="mt-0.5 h-3.5 w-3.5 shrink-0 text-teal-700" />
                    <div className="min-w-0 flex-1">
                      <div className="truncate text-[11px] font-semibold text-ink-900">{e.qualification}</div>
                      <div className="text-[9px] text-slate-400">{e.institution}{e.year ? ` · ${e.year}` : ''}</div>
                    </div>
                    <button onClick={() => setEducation((x) => x.filter((_, idx) => idx !== i))} className="text-slate-300 hover:text-rose-500"><Trash2 className="h-3.5 w-3.5" /></button>
                  </div>
                ))}
                {education.length === 0 && <div className="rounded-xl border border-dashed border-teal-900/15 p-2.5 text-center text-[10px] text-slate-400">No qualifications added.</div>}
              </div>
              <div className="mt-3 grid gap-2 sm:grid-cols-2">
                <Input placeholder="Qualification (MBChB)" value={newEdu.qualification} onChange={(e) => setNewEdu({ ...newEdu, qualification: e.target.value })} />
                <Input placeholder="Institution" value={newEdu.institution} onChange={(e) => setNewEdu({ ...newEdu, institution: e.target.value })} />
                <Input placeholder="Year" type="number" value={newEdu.year} onChange={(e) => setNewEdu({ ...newEdu, year: e.target.value })} />
                <Button
                  size="sm"
                  variant="outline"
                  icon={<Plus className="h-3.5 w-3.5" />}
                  onClick={() => {
                    if (!newEdu.qualification.trim()) return;
                    setEducation((x) => [...x, { ...newEdu, year: newEdu.year ? Number(newEdu.year) : undefined }]);
                    setNewEdu({ qualification: '', institution: '', year: '' });
                  }}
                >
                  Add
                </Button>
              </div>
            </section>
          </div>
        </div>
      )}

      {tab === 'practice' && hirer && (
        <section className="card p-4 sm:p-5">
          <SectionTitle eyebrow="Hiring" title="Practice details" sub="Shared with professionals only after a booking is accepted." />
          <div className="grid gap-3 sm:grid-cols-2">
            <Field label="Practice / hospital"><Input value={practice.practiceName} onChange={(e) => setPractice({ ...practice, practiceName: e.target.value })} /></Field>
            <Field label="Practice type">
              <Select value={practice.practiceType} onChange={(e) => setPractice({ ...practice, practiceType: e.target.value })}>
                {['Private Practice', 'Private Hospital', 'Public Hospital', 'Clinic', 'Day Hospital', 'Agency'].map((p) => <option key={p}>{p}</option>)}
              </Select>
            </Field>
            <Field label="BHF / registration number"><Input value={practice.registrationNumber} onChange={(e) => setPractice({ ...practice, registrationNumber: e.target.value })} placeholder="BHF 0045121" /></Field>
            <Field label="Contact person"><Input value={practice.contactPerson} onChange={(e) => setPractice({ ...practice, contactPerson: e.target.value })} /></Field>
            <Field label="City">
              <Select value={practice.city} onChange={(e) => setPractice({ ...practice, city: e.target.value })}>
                {CITIES.map((c) => <option key={c}>{c}</option>)}
              </Select>
            </Field>
            <Field label="Staff count"><Input type="number" min={0} value={practice.staffCount} onChange={(e) => setPractice({ ...practice, staffCount: e.target.value })} /></Field>
            <Field label="Full address" className="sm:col-span-2" hint="Only revealed to a professional once you approve their booking">
              <Input value={practice.address} onChange={(e) => setPractice({ ...practice, address: e.target.value })} placeholder="Netcare Park Lane, 1 Junction Avenue, Parktown, Johannesburg, 2193" />
            </Field>
          </div>
          <div className="mt-4 flex items-center justify-between border-t border-teal-900/6 pt-3">
            <div className="flex items-center gap-1.5 text-[10px] text-slate-400"><Building2 className="h-3 w-3" /> Used for escrow receipts and shift notices.</div>
            <Button size="sm" loading={busy} onClick={savePractice} icon={<Save className="h-3.5 w-3.5" />}>Save practice</Button>
          </div>
        </section>
      )}

      {tab === 'verification' && (
        <div className="grid gap-5 lg:grid-cols-[1.3fr_1fr]">
          <section className="card p-4 sm:p-5">
            <SectionTitle
              eyebrow="Certifications"
              title="Registration & credentials"
              sub="Upload HPCSA, SANC or BHF proof. Documents are private and only shown as a verified badge."
              action={<Badge tone={verifiedCerts ? 'emerald' : 'amber'}>{verifiedCerts}/{certs.length} verified</Badge>}
            />
            <div className="space-y-2">
              {certs.map((c, i) => (
                <div key={i} className="flex items-center gap-2.5 rounded-xl border border-teal-900/8 bg-white px-3 py-2.5">
                  <span className={cn('grid h-8 w-8 shrink-0 place-items-center rounded-lg', c.verified ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700')}>
                    <Award className="h-4 w-4" />
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[12px] font-semibold text-ink-900">{c.name}</div>
                    <div className="text-[9px] text-slate-400">{c.issuer || 'Issuer pending'}{c.number ? ` · ${c.number}` : ''}{c.year ? ` · ${c.year}` : ''}</div>
                  </div>
                  {c.url && <a href={c.url} target="_blank" rel="noreferrer" className="text-[10px] font-semibold text-teal-700 hover:underline"><FileText className="h-3.5 w-3.5" /></a>}
                  <Badge tone={c.verified ? 'emerald' : 'amber'}>{c.verified ? 'Verified' : 'Pending'}</Badge>
                  <button onClick={() => setCerts((x) => x.filter((_, idx) => idx !== i))} className="text-slate-300 hover:text-rose-500"><Trash2 className="h-3.5 w-3.5" /></button>
                </div>
              ))}
              {certs.length === 0 && <div className="rounded-xl border border-dashed border-teal-900/15 p-4 text-center text-[11px] text-slate-400">No certifications yet. Upload your HPCSA or SANC proof to get verified.</div>}
            </div>

            <div className="mt-4 rounded-2xl border border-teal-900/10 bg-mint-50/60 p-3">
              <div className="mb-2 text-[11px] font-bold text-ink-900">Add a certification</div>
              <div className="grid gap-2 sm:grid-cols-2">
                <Input placeholder="Name (HPCSA Registration)" value={newCert.name} onChange={(e) => setNewCert({ ...newCert, name: e.target.value })} />
                <Input placeholder="Number (MP 0451237)" value={newCert.number} onChange={(e) => setNewCert({ ...newCert, number: e.target.value })} />
                <Input placeholder="Issuer (HPCSA / SANC)" value={newCert.issuer} onChange={(e) => setNewCert({ ...newCert, issuer: e.target.value })} />
                <Input placeholder="Year" type="number" value={newCert.year} onChange={(e) => setNewCert({ ...newCert, year: e.target.value })} />
              </div>
              <div className="mt-2 flex flex-wrap gap-2">
                <input ref={fileRef} type="file" accept="application/pdf,image/png,image/jpeg,image/webp" onChange={onFile} className="hidden" id="cert-upload" />
                <Button size="sm" variant="primary" loading={uploading} onClick={() => fileRef.current?.click()} icon={<Upload className="h-3.5 w-3.5" />}>Upload document</Button>
                <Button
                  size="sm" variant="outline" icon={<Plus className="h-3.5 w-3.5" />}
                  onClick={() => {
                    if (!newCert.name.trim()) return toast('Give the certification a name.', 'error');
                    setCerts((c) => [...c, { ...newCert, year: newCert.year ? Number(newCert.year) : undefined, verified: false }]);
                    setNewCert({ name: '', number: '', issuer: '', year: '' });
                    toast('Certification added — remember to save.', 'info');
                  }}
                >
                  Add without file
                </Button>
                <Button size="sm" variant="soft" loading={busy} onClick={savePro} icon={<Save className="h-3.5 w-3.5" />}>Save</Button>
              </div>
              <p className="mt-2 text-[9px] leading-relaxed text-slate-400">PDF, PNG, JPG or WEBP · max 6 MB. In this demo, verification is approved by the MediGig team.</p>
            </div>
          </section>

          <div className="space-y-5">
            <section className="relative overflow-hidden rounded-[24px] bg-ink-950 p-5 text-white">
              <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-brand-500/20 blur-3xl" />
              <div className="relative">
                <div className="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-[0.16em] text-brand-400"><ShieldCheck className="h-3 w-3" /> POPIA</div>
                <h3 className="mt-2 text-lg font-extrabold tracking-tight">Your documents stay private.</h3>
                <p className="mt-1.5 text-[11px] leading-relaxed text-mint-100/70">
                  Certification files are only accessible to you and the MediGig verification team. Hirers see a verified badge, never the document.
                </p>
                <ul className="mt-3 space-y-1.5 text-[10px] text-mint-100/60">
                  {['HPCSA / SANC numbers checked against the register', 'No patient data ever uploaded', 'Addresses unlock only after acceptance'].map((x) => (
                    <li key={x} className="flex items-center gap-1.5"><span className="h-1 w-1 rounded-full bg-brand-400" /> {x}</li>
                  ))}
                </ul>
              </div>
            </section>

            <section className="card p-4 sm:p-5">
              <SectionTitle eyebrow="Checklist" title="Get fully verified" />
              <div className="space-y-2">
                {[
                  { k: 'Profile details complete', ok: profileSteps.every((s) => s.ok) },
                  { k: 'Certification uploaded', ok: certs.length > 0 },
                  { k: 'Issuer recorded', ok: certs.some((c) => c.issuer) },
                  { k: 'At least one document verified', ok: verifiedCerts > 0 },
                ].map((s) => (
                  <div key={s.k} className="flex items-center gap-2 text-[11px]">
                    <span className={cn('grid h-4 w-4 place-items-center rounded-full', s.ok ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-400')}>
                      {s.ok ? <BadgeCheck className="h-2.5 w-2.5" /> : <span className="h-1 w-1 rounded-full bg-current" />}
                    </span>
                    <span className={s.ok ? 'text-slate-500 line-through decoration-slate-300' : 'font-medium text-ink-800'}>{s.k}</span>
                  </div>
                ))}
              </div>
            </section>

            <section className="card p-4 sm:p-5">
              <SectionTitle eyebrow="Visibility" title="What others see" />
              <div className="space-y-2 text-[11px]">
                {[
                  { k: 'Name & initials', v: 'Visible in search' },
                  { k: 'Profession & rate', v: 'Visible in search' },
                  { k: 'Distance', v: 'Shown in km only' },
                  { k: 'Full address', v: 'After acceptance' },
                  { k: 'Phone number', v: 'After acceptance' },
                  { k: 'Certification files', v: 'Private' },
                ].map((x) => (
                  <div key={x.k} className="flex items-center justify-between border-b border-teal-900/5 pb-1.5 last:border-0">
                    <span className="text-slate-500">{x.k}</span>
                    <span className="font-semibold text-ink-900">{x.v}</span>
                  </div>
                ))}
              </div>
            </section>
          </div>
        </div>
      )}
    </div>
  );
}
