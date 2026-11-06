import React, { useEffect, useMemo, useRef, useState } from 'react';
import { motion, useReducedMotion } from 'motion/react';
import { Check, Loader2, Sparkles } from 'lucide-react';
import { cn } from '../lib/utils';

/* ── background ─────────────────────────────────────────────────────── */
export function GridBackground({ className }: { className?: string }) {
  return (
    <div aria-hidden className={cn('pointer-events-none absolute inset-0 overflow-hidden', className)}>
      <div className="absolute inset-0 grid-bg opacity-70" />
      <div className="absolute -left-32 top-[-10%] h-[420px] w-[420px] rounded-full bg-teal-300/30 blur-[110px] animate-drift" />
      <div className="absolute right-[-10%] top-[20%] h-[360px] w-[360px] rounded-full bg-amber-200/40 blur-[120px] animate-drift" style={{ animationDelay: '-8s' }} />
      <div className="absolute bottom-[-20%] left-[35%] h-[420px] w-[420px] rounded-full bg-emerald-200/30 blur-[120px] animate-drift" style={{ animationDelay: '-15s' }} />
    </div>
  );
}

export function GradientText({ children, className }: { children: React.ReactNode; className?: string }) {
  return <span className={cn('gradient-text', className)}>{children}</span>;
}

/* ── reveal ─────────────────────────────────────────────────────────── */
export function Reveal({ children, delay = 0, y = 18, className }: { children: React.ReactNode; delay?: number; y?: number; className?: string }) {
  const reduce = useReducedMotion();
  if (reduce) return <div className={className}>{children}</div>;
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.05 }}
      transition={{ duration: 0.65, delay, ease: [0.22, 1, 0.36, 1] }}
    >
      {children}
    </motion.div>
  );
}

/* ── tilt card ──────────────────────────────────────────────────────── */
export function TiltCard({ children, className, intensity = 7 }: { children: React.ReactNode; className?: string; intensity?: number }) {
  const ref = useRef<HTMLDivElement>(null);
  const [style, setStyle] = useState<React.CSSProperties>({});
  const reduce = useReducedMotion();
  const onMove = (e: React.MouseEvent) => {
    if (reduce || !ref.current) return;
    const r = ref.current.getBoundingClientRect();
    const px = (e.clientX - r.left) / r.width - 0.5;
    const py = (e.clientY - r.top) / r.height - 0.5;
    setStyle({ transform: `perspective(900px) rotateX(${(-py * intensity).toFixed(2)}deg) rotateY(${(px * intensity).toFixed(2)}deg) translateY(-2px)` });
  };
  const onLeave = () => setStyle({ transform: 'perspective(900px) rotateX(0) rotateY(0)' });
  return (
    <div
      ref={ref}
      onMouseMove={onMove}
      onMouseLeave={onLeave}
      style={{ ...style, transition: 'transform .35s cubic-bezier(.22,1,.36,1)' }}
      className={className}
    >
      {children}
    </div>
  );
}

/* ── number ticker ──────────────────────────────────────────────────── */
export function NumberTicker({ value, format, className, duration = 900 }: { value: number; format?: (n: number) => string; className?: string; duration?: number }) {
  const [display, setDisplay] = useState(value);
  const fromRef = useRef(value);
  const rafRef = useRef<number | null>(null);
  const reduce = useReducedMotion();
  useEffect(() => {
    if (reduce) { setDisplay(value); return; }
    const from = fromRef.current;
    const to = value;
    if (from === to) return;
    const start = performance.now();
    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      setDisplay(from + (to - from) * eased);
      if (t < 1) rafRef.current = requestAnimationFrame(tick);
      else fromRef.current = to;
    };
    rafRef.current = requestAnimationFrame(tick);
    return () => { if (rafRef.current) cancelAnimationFrame(rafRef.current); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, duration, reduce]);
  const text = format ? format(display) : Math.round(display).toLocaleString('en-ZA');
  return <span className={className}>{text}</span>;
}

/* ── typewriter ─────────────────────────────────────────────────────── */
export function useTypewriter(text: string, speed = 9) {
  const [out, setOut] = useState('');
  const reduce = useReducedMotion();
  useEffect(() => {
    if (reduce) { setOut(text); return; }
    setOut('');
    let i = 0;
    const t = setInterval(() => {
      i += Math.max(1, Math.round(text.length / 240));
      setOut(text.slice(0, i));
      if (i >= text.length) clearInterval(t);
    }, speed);
    return () => clearInterval(t);
  }, [text, speed, reduce]);
  return out;
}

/* ── thinking state (affectUI-inspired) ─────────────────────────────── */
const DEFAULT_PHASES = ['Reading your workspace', 'Checking availability', 'Ranking by distance', 'Writing a reply'];

export function ThinkingState({ active, phases = DEFAULT_PHASES, startedAt, className }: { active: boolean; phases?: string[]; startedAt?: number; className?: string }) {
  const [phase, setPhase] = useState(0);
  const [seconds, setSeconds] = useState(0);
  useEffect(() => {
    if (!active) return;
    setPhase(0);
    setSeconds(0);
    const start = startedAt || Date.now();
    const p = setInterval(() => setPhase((v) => (v + 1) % phases.length), 1400);
    const s = setInterval(() => setSeconds(Math.max(0, Math.round((Date.now() - start) / 1000))), 500);
    return () => { clearInterval(p); clearInterval(s); };
  }, [active, phases.length, startedAt]);
  if (!active) return null;
  return (
    <div className={cn('rounded-2xl border border-teal-900/10 bg-white/75 p-3.5', className)}>
      <div className="flex items-center gap-2.5">
        <span className="relative grid h-7 w-7 place-items-center rounded-full bg-mint-100 text-teal-700">
          <span className="absolute inset-0 rounded-full bg-teal-400/30" style={{ animation: 'pulseRing 1.8s cubic-bezier(0,0,.2,1) infinite' }} />
          <Sparkles className="h-3.5 w-3.5" />
        </span>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2 text-[13px] font-semibold text-ink-900">
            <span key={phase} className="truncate">{phases[phase]}<span className="caret" /></span>
            <span className="ml-auto shrink-0 font-mono text-[11px] font-medium text-slate-400">{seconds}s</span>
          </div>
          <div className="mt-1.5 h-1 w-full overflow-hidden rounded-full bg-teal-900/8">
            <motion.div
              key={phase}
              initial={{ x: '-100%' }}
              animate={{ x: '100%' }}
              transition={{ duration: 1.4, repeat: Infinity, ease: 'linear' }}
              className="h-full w-1/2 rounded-full bg-gradient-to-r from-transparent via-teal-400 to-transparent"
            />
          </div>
        </div>
      </div>
    </div>
  );
}

/* ── tool call row ──────────────────────────────────────────────────── */
export function ToolCallRow({ label, detail, status = 'done', index = 0 }: { label: string; detail?: string; status?: 'running' | 'done' | 'failed'; index?: number }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.12 }}
      className="flex items-center gap-2.5 rounded-xl border border-teal-900/8 bg-white/70 px-3 py-2"
    >
      {status === 'running'
        ? <Loader2 className="h-3.5 w-3.5 animate-spin text-teal-600" />
        : <span className={cn('grid h-5 w-5 place-items-center rounded-full', status === 'failed' ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-700')}><Check className="h-3 w-3" /></span>}
      <span className="font-mono text-[11px] font-semibold text-ink-900">{label}</span>
      {detail && <span className="ml-auto truncate font-mono text-[10px] text-slate-400">{detail}</span>}
    </motion.div>
  );
}

/* ── marquee ────────────────────────────────────────────────────────── */
export function Marquee({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={cn('relative flex overflow-hidden', className)}>
      <div className="flex min-w-full shrink-0 animate-marquee items-center gap-3">
        {children}{children}
      </div>
    </div>
  );
}

/* ── charts ─────────────────────────────────────────────────────────── */
export function EarningsChart({ data, height = 150, format }: { data: { label: string; value: number }[]; height?: number; format?: (n: number) => string }) {
  const max = Math.max(1, ...data.map((d) => d.value));
  const [hover, setHover] = useState<number | null>(null);
  return (
    <div className="w-full">
      <div className="flex items-end gap-2" style={{ height }}>
        {data.map((d, i) => {
          const h = Math.max(4, (d.value / max) * 100);
          return (
            <div key={d.label + i} className="group relative flex h-full flex-1 flex-col justify-end">
              <div className="relative flex h-full flex-col justify-end">
                {hover === i && (
                  <div className="absolute -top-1 left-1/2 z-10 -translate-x-1/2 -translate-y-full whitespace-nowrap rounded-lg bg-ink-900 px-2 py-1 text-[10px] font-semibold text-white shadow">
                    {format ? format(d.value) : d.value}
                  </div>
                )}
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: `${h}%` }}
                  transition={{ duration: 0.8, delay: i * 0.05, ease: [0.22, 1, 0.36, 1] }}
                  onMouseEnter={() => setHover(i)}
                  onMouseLeave={() => setHover(null)}
                  className={cn('w-full rounded-t-lg transition-colors', i === data.length - 1 ? 'bg-gradient-to-t from-teal-700 to-teal-400' : 'bg-teal-200/80 hover:bg-teal-300')}
                />
              </div>
              <div className="mt-2 text-center text-[10px] font-medium text-slate-400">{d.label}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function Donut({ value, size = 84, stroke = 8, label }: { value: number; size?: number; stroke?: number; label?: string }) {
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  return (
    <div className="relative grid place-items-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="rgba(15,118,110,.10)" strokeWidth={stroke} />
        <motion.circle
          cx={size / 2} cy={size / 2} r={r} fill="none" stroke="url(#donutGrad)" strokeWidth={stroke} strokeLinecap="round"
          strokeDasharray={c} initial={{ strokeDashoffset: c }} animate={{ strokeDashoffset: c - (c * Math.max(0, Math.min(100, value))) / 100 }}
          transition={{ duration: 1.1, ease: [0.22, 1, 0.36, 1] }}
        />
        <defs>
          <linearGradient id="donutGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#2dd4bf" />
            <stop offset="100%" stopColor="#0f766e" />
          </linearGradient>
        </defs>
      </svg>
      <div className="absolute text-center">
        <div className="text-base font-bold leading-none text-ink-950">{label ?? `${Math.round(value)}%`}</div>
      </div>
    </div>
  );
}

/* ── static demo mini map ───────────────────────────────────────────── */
export function MiniMap({ points = [], className }: { points?: { x: number; y: number; label: string }[]; className?: string }) {
  const dots = useMemo(() => points, [points]);
  return (
    <div className={cn('relative overflow-hidden rounded-2xl border border-teal-900/10 bg-[#e9f6f0]', className)}>
      <svg viewBox="0 0 400 200" className="absolute inset-0 h-full w-full" aria-hidden>
        <defs>
          <pattern id="mapgrid" width="28" height="28" patternUnits="userSpaceOnUse">
            <path d="M28 0H0v28" fill="none" stroke="rgba(15,118,110,.08)" strokeWidth="1" />
          </pattern>
          <linearGradient id="mapfade" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#d9f3ec" /><stop offset="100%" stopColor="#eefaf5" />
          </linearGradient>
        </defs>
        <rect width="400" height="200" fill="url(#mapfade)" />
        <rect width="400" height="200" fill="url(#mapgrid)" />
        <path d="M-10 150 C60 120 90 170 150 140 S250 90 310 120 420 80 430 90" fill="none" stroke="rgba(15,118,110,.20)" strokeWidth="14" strokeLinecap="round" />
        <path d="M40 -10 C70 50 40 80 80 130 S150 190 130 220" fill="none" stroke="rgba(15,118,110,.14)" strokeWidth="10" strokeLinecap="round" />
        <path d="M230 -10 C240 40 300 50 300 110 S340 180 330 220" fill="none" stroke="rgba(15,118,110,.12)" strokeWidth="8" strokeLinecap="round" />
      </svg>
      {dots.map((p, i) => (
        <div key={i} className="absolute -translate-x-1/2 -translate-y-1/2" style={{ left: `${p.x}%`, top: `${p.y}%` }}>
          <span className="relative grid h-7 w-7 place-items-center rounded-full bg-white text-[9px] font-bold text-teal-800 shadow-lg ring-2 ring-teal-500/30">
            {p.label}
            <span className="absolute inset-0 rounded-full bg-teal-400/30" style={{ animation: 'pulseRing 2.4s ease-out infinite' }} />
          </span>
        </div>
      ))}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-white/40 to-transparent" />
    </div>
  );
}
