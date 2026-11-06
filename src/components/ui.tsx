import React, { useEffect, useRef } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { X } from 'lucide-react';
import { cn, initialsOf } from '../lib/utils';

/* ── Avatar ─────────────────────────────────────────────────────────── */
const AVATAR_GRADIENTS = [
  'from-teal-500 to-emerald-700',
  'from-cyan-500 to-teal-700',
  'from-emerald-500 to-teal-800',
  'from-sky-500 to-teal-600',
  'from-amber-400 to-teal-600',
  'from-rose-400 to-teal-700',
  'from-indigo-400 to-teal-600',
];

export function Avatar({
  name, size = 'md', className, ring = false, online = false,
}: { name: string; size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'; className?: string; ring?: boolean; online?: boolean }) {
  const initials = initialsOf(name);
  const idx = [...name].reduce((s, c) => s + c.charCodeAt(0), 0) % AVATAR_GRADIENTS.length;
  const sizes = {
    xs: 'h-7 w-7 text-[10px]',
    sm: 'h-9 w-9 text-xs',
    md: 'h-11 w-11 text-sm',
    lg: 'h-14 w-14 text-base',
    xl: 'h-20 w-20 text-xl',
  };
  return (
    <div className={cn('relative shrink-0', className)}>
      <div className={cn(
        'grid place-items-center rounded-full bg-gradient-to-br font-semibold text-white shadow-sm',
        AVATAR_GRADIENTS[idx], sizes[size], ring && 'ring-2 ring-white ring-offset-2 ring-offset-mint-50',
      )}>
        {initials || 'MG'}
      </div>
      {online && <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-white bg-emerald-500" />}
    </div>
  );
}

/* ── Badge ──────────────────────────────────────────────────────────── */
export function Badge({ children, tone = 'neutral', className }: { children: React.ReactNode; tone?: string; className?: string }) {
  const tones: Record<string, string> = {
    neutral: 'bg-slate-100 text-slate-600 border-slate-200',
    teal: 'bg-teal-50 text-teal-800 border-teal-200',
    emerald: 'bg-emerald-50 text-emerald-800 border-emerald-200',
    amber: 'bg-amber-50 text-amber-800 border-amber-200',
    rose: 'bg-rose-50 text-rose-700 border-rose-200',
    sky: 'bg-sky-50 text-sky-800 border-sky-200',
    violet: 'bg-violet-50 text-violet-800 border-violet-200',
    dark: 'bg-ink-900 text-mint-100 border-ink-800',
    white: 'bg-white/80 text-ink-800 border-white',
  };
  return (
    <span className={cn('inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-semibold tracking-wide', tones[tone] || tones.neutral, className)}>
      {children}
    </span>
  );
}

/* ── Button ─────────────────────────────────────────────────────────── */
type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'brand' | 'soft' | 'outline' | 'ghost' | 'amber' | 'danger' | 'white';
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'icon';
  loading?: boolean;
  icon?: React.ReactNode;
};

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { className, variant = 'primary', size = 'md', loading, icon, children, disabled, ...props }, ref,
) {
  const variants: Record<string, string> = {
    primary: 'bg-ink-900 text-white hover:bg-ink-800 shadow-[0_12px_30px_-16px_rgba(4,33,29,.9)]',
    brand: 'bg-brand-600 text-white hover:bg-brand-700 shadow-[0_12px_30px_-16px_rgba(13,148,136,.9)]',
    soft: 'bg-mint-100 text-ink-900 hover:bg-mint-200',
    outline: 'border border-teal-900/12 bg-white/70 text-ink-900 hover:bg-white hover:border-teal-900/25',
    ghost: 'text-ink-700 hover:bg-mint-100/70',
    amber: 'bg-amber-400 text-ink-950 hover:bg-amber-300 shadow-[0_12px_30px_-16px_rgba(245,158,11,.9)]',
    danger: 'bg-rose-600 text-white hover:bg-rose-700',
    white: 'bg-white text-ink-900 hover:bg-mint-50 border border-white/40',
  };
  const sizes: Record<string, string> = {
    xs: 'h-8 px-3 text-xs rounded-full gap-1.5',
    sm: 'h-9 px-4 text-[13px] rounded-full gap-1.5',
    md: 'h-11 px-5 text-sm rounded-full gap-2',
    lg: 'h-12 px-6 text-[15px] rounded-full gap-2',
    icon: 'h-10 w-10 rounded-full justify-center',
  };
  return (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={cn(
        'inline-flex select-none items-center justify-center font-semibold transition-all duration-200 active:scale-[.98] disabled:cursor-not-allowed disabled:opacity-50',
        variants[variant], sizes[size], className,
      )}
      {...props}
    >
      {loading ? <Spinner className={cn('h-4 w-4', variant === 'primary' || variant === 'brand' || variant === 'danger' ? 'text-white' : 'text-teal-700')} /> : icon}
      {children}
    </button>
  );
});

export function IconButton({ className, children, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      className={cn('grid h-10 w-10 place-items-center rounded-full border border-teal-900/10 bg-white/80 text-ink-800 transition hover:bg-white hover:border-teal-900/25 active:scale-95', className)}
      {...props}
    >
      {children}
    </button>
  );
}

/* ── Spinner / progress ─────────────────────────────────────────────── */
export function Spinner({ className }: { className?: string }) {
  return (
    <svg className={cn('animate-spin', className)} viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2.5" opacity=".18" />
      <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}

export function Progress({ value, className, tone = 'bg-brand-500' }: { value: number; className?: string; tone?: string }) {
  return (
    <div className={cn('h-2 w-full overflow-hidden rounded-full bg-teal-900/8', className)}>
      <motion.div
        initial={{ width: 0 }}
        animate={{ width: `${Math.max(0, Math.min(100, value))}%` }}
        transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
        className={cn('h-full rounded-full', tone)}
      />
    </div>
  );
}

/* ── Form fields ────────────────────────────────────────────────────── */
export function Field({ label, hint, error, children, className }: { label?: string; hint?: string; error?: string; children: React.ReactNode; className?: string }) {
  return (
    <label className={cn('block', className)}>
      {label && <span className="mb-1.5 block text-[12px] font-semibold uppercase tracking-[0.12em] text-ink-700/70">{label}</span>}
      {children}
      {hint && !error && <span className="mt-1 block text-xs text-slate-500">{hint}</span>}
      {error && <span className="mt-1 block text-xs font-medium text-rose-600">{error}</span>}
    </label>
  );
}

export const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(function Input({ className, ...props }, ref) {
  return (
    <input
      ref={ref}
      className={cn('h-11 w-full rounded-xl border border-teal-900/12 bg-white/85 px-3.5 text-sm text-ink-950 outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/12', className)}
      {...props}
    />
  );
});

export function Textarea({ className, ...props }: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={cn('min-h-[96px] w-full resize-y rounded-xl border border-teal-900/12 bg-white/85 px-3.5 py-3 text-sm text-ink-950 outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/12', className)}
      {...props}
    />
  );
}

export function Select({ className, children, ...props }: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={cn('h-11 w-full appearance-none rounded-xl border border-teal-900/12 bg-white/85 px-3.5 text-sm text-ink-950 outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-500/12', className)}
      {...props}
    >
      {children}
    </select>
  );
}

export function Toggle({ checked, onChange, label }: { checked: boolean; onChange: (v: boolean) => void; label?: string }) {
  return (
    <button
      type="button"
      onClick={() => onChange(!checked)}
      className="inline-flex items-center gap-2.5 text-sm font-medium text-ink-800"
    >
      <span className={cn('relative h-6 w-11 rounded-full transition-colors duration-300', checked ? 'bg-brand-600' : 'bg-slate-300')}>
        <span className={cn('absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all duration-300', checked ? 'left-[22px]' : 'left-0.5')} />
      </span>
      {label}
    </button>
  );
}

/* ── Stars ──────────────────────────────────────────────────────────── */
export function Stars({ value = 0, size = 14, className }: { value?: number; size?: number; className?: string }) {
  return (
    <span className={cn('inline-flex items-center gap-0.5 text-amber-400', className)} aria-label={`${value} out of 5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <svg key={i} width={size} height={size} viewBox="0 0 20 20" fill={i <= Math.round(value) ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth="1.4">
          <path d="M10 1.8l2.5 5.1 5.6.8-4 4 .9 5.6-5-2.7-5 2.7.9-5.6-4-4 5.6-.8z" strokeLinejoin="round" />
        </svg>
      ))}
    </span>
  );
}

/* ── Modal ──────────────────────────────────────────────────────────── */
export function Modal({
  open, onClose, title, subtitle, children, footer, size = 'md', className,
}: {
  open: boolean;
  onClose: () => void;
  title?: React.ReactNode;
  subtitle?: React.ReactNode;
  children: React.ReactNode;
  footer?: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}) {
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => { document.body.style.overflow = prev; window.removeEventListener('keydown', onKey); };
  }, [open, onClose]);

  const widths = { sm: 'max-w-md', md: 'max-w-xl', lg: 'max-w-3xl', xl: 'max-w-5xl' };
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="fixed inset-0 z-[80] flex items-end justify-center p-0 sm:items-center sm:p-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
          <div className="absolute inset-0 bg-ink-950/55 backdrop-blur-[3px]" onClick={onClose} />
          <motion.div
            role="dialog"
            aria-modal
            initial={{ y: 30, opacity: 0, scale: 0.98 }}
            animate={{ y: 0, opacity: 1, scale: 1 }}
            exit={{ y: 24, opacity: 0, scale: 0.98 }}
            transition={{ type: 'spring', stiffness: 320, damping: 30 }}
            className={cn('relative z-10 flex max-h-[92vh] w-full flex-col overflow-hidden rounded-t-[28px] bg-[#fbfdfc] shadow-2xl sm:rounded-[28px]', widths[size], className)}
          >
            <div className="flex items-start justify-between gap-4 border-b border-teal-900/8 px-5 py-4 sm:px-6">
              <div>
                {title && <h2 className="text-lg font-bold tracking-tight text-ink-950 sm:text-xl">{title}</h2>}
                {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
              </div>
              <IconButton onClick={onClose} aria-label="Close"><X className="h-4 w-4" /></IconButton>
            </div>
            <div className="hide-scrollbar flex-1 overflow-y-auto px-5 py-5 sm:px-6">{children}</div>
            {footer && <div className="border-t border-teal-900/8 bg-white/70 px-5 py-4 sm:px-6">{footer}</div>}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

/* ── Drawer ─────────────────────────────────────────────────────────── */
export function Drawer({ open, onClose, children, side = 'right', className }: { open: boolean; onClose: () => void; children: React.ReactNode; side?: 'right' | 'left'; className?: string }) {
  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = prev; };
  }, [open]);
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="fixed inset-0 z-[85]" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
          <div className="absolute inset-0 bg-ink-950/45 backdrop-blur-[2px]" onClick={onClose} />
          <motion.aside
            initial={{ x: side === 'right' ? '100%' : '-100%' }}
            animate={{ x: 0 }}
            exit={{ x: side === 'right' ? '100%' : '-100%' }}
            transition={{ type: 'spring', stiffness: 320, damping: 34 }}
            className={cn('absolute inset-y-0 w-full max-w-lg overflow-hidden bg-[#fbfdfc] shadow-2xl', side === 'right' ? 'right-0' : 'left-0', className)}
          >
            {children}
          </motion.aside>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

/* ── Stat / section ─────────────────────────────────────────────────── */
export function StatCard({ label, value, sub, icon, tone = 'mint' }: { label: string; value: React.ReactNode; sub?: React.ReactNode; icon?: React.ReactNode; tone?: 'mint' | 'sand' | 'white' | 'dark' }) {
  const tones = {
    mint: 'bg-mint-100/70 border-mint-200/60 text-ink-950',
    sand: 'bg-sand-100/70 border-sand-300/50 text-ink-950',
    white: 'bg-white/80 border-teal-900/8 text-ink-950',
    dark: 'bg-ink-900 border-ink-800 text-white',
  };
  return (
    <div className={cn('relative overflow-hidden rounded-2xl border p-4', tones[tone])}>
      <div className="flex items-start justify-between gap-2">
        <span className={cn('text-[11px] font-semibold uppercase tracking-[0.13em]', tone === 'dark' ? 'text-mint-200/70' : 'text-ink-700/60')}>{label}</span>
        {icon && <span className={cn('grid h-8 w-8 place-items-center rounded-full', tone === 'dark' ? 'bg-white/10 text-mint-100' : 'bg-white/70 text-teal-700')}>{icon}</span>}
      </div>
      <div className="mt-3 text-[26px] font-bold leading-none tracking-tight">{value}</div>
      {sub && <div className={cn('mt-2 text-xs', tone === 'dark' ? 'text-mint-200/70' : 'text-slate-500')}>{sub}</div>}
    </div>
  );
}

export function SectionTitle({ eyebrow, title, sub, action }: { eyebrow?: string; title: React.ReactNode; sub?: React.ReactNode; action?: React.ReactNode }) {
  return (
    <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
      <div>
        {eyebrow && <div className="mb-1 text-[11px] font-bold uppercase tracking-[0.18em] text-brand-700/70">{eyebrow}</div>}
        <h2 className="text-lg font-bold tracking-tight text-ink-950 sm:text-xl">{title}</h2>
        {sub && <p className="mt-1 max-w-2xl text-sm text-slate-500">{sub}</p>}
      </div>
      {action}
    </div>
  );
}

export function EmptyState({ icon, title, body, action }: { icon?: React.ReactNode; title: string; body?: string; action?: React.ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-teal-900/15 bg-white/50 px-6 py-12 text-center">
      <div className="mb-3 grid h-12 w-12 place-items-center rounded-full bg-mint-100 text-teal-700">{icon}</div>
      <h3 className="text-sm font-bold text-ink-950">{title}</h3>
      {body && <p className="mt-1 max-w-sm text-xs leading-relaxed text-slate-500">{body}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

export function Segmented<T extends string>({ value, onChange, options, className }: { value: T; onChange: (v: T) => void; options: { value: T; label: string; icon?: React.ReactNode }[]; className?: string }) {
  return (
    <div className={cn('inline-flex rounded-full border border-teal-900/10 bg-white/70 p-1', className)}>
      {options.map((o) => (
        <button
          key={o.value}
          type="button"
          onClick={() => onChange(o.value)}
          className={cn(
            'relative inline-flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-[12px] font-semibold transition',
            value === o.value ? 'bg-ink-900 text-white shadow' : 'text-ink-700/70 hover:text-ink-900',
          )}
        >
          {o.icon}{o.label}
        </button>
      ))}
    </div>
  );
}

export function Toaster({ toasts, dismiss }: { toasts: { id: string; text: string; tone: string }[]; dismiss: (id: string) => void }) {
  return (
    <div className="pointer-events-none fixed inset-x-0 bottom-24 z-[95] flex flex-col items-center gap-2 px-4 sm:bottom-8 sm:right-6 sm:left-auto sm:items-end">
      <AnimatePresence>
        {toasts.map((t) => (
          <motion.div
            key={t.id}
            layout
            initial={{ opacity: 0, y: 12, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 8, scale: 0.96 }}
            onClick={() => dismiss(t.id)}
            className={cn(
              'pointer-events-auto flex max-w-sm cursor-pointer items-center gap-3 rounded-2xl border px-4 py-3 text-sm font-medium shadow-xl backdrop-blur',
              t.tone === 'error' ? 'border-rose-200 bg-rose-50 text-rose-800'
                : t.tone === 'success' ? 'border-emerald-200 bg-white text-ink-900'
                  : 'border-teal-900/10 bg-white text-ink-900',
            )}
          >
            <span className={cn('h-2 w-2 shrink-0 rounded-full', t.tone === 'error' ? 'bg-rose-500' : t.tone === 'success' ? 'bg-emerald-500' : 'bg-teal-500')} />
            {t.text}
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
}

export function Skeleton({ className }: { className?: string }) {
  return <div className={cn('shimmer rounded-xl', className)} />;
}

export function Logo({ dark = false, compact = false }: { dark?: boolean; compact?: boolean }) {
  return (
    <div className="flex items-center gap-2.5">
      <span className={cn('grid h-9 w-9 place-items-center rounded-[12px] shadow-sm', dark ? 'bg-white text-ink-900' : 'bg-ink-900 text-white')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
          <path d="M9.5 3h5v6.5H21v5h-6.5V21h-5v-6.5H3v-5h6.5V3z" fill="currentColor" />
        </svg>
      </span>
      {!compact && (
        <span className={cn('text-[17px] font-extrabold tracking-tight', dark ? 'text-white' : 'text-ink-950')}>
          MediGig<span className="text-brand-500"> ZA</span>
        </span>
      )}
    </div>
  );
}

export function useClickOutside<T extends HTMLElement>(onOutside: () => void) {
  const ref = useRef<T | null>(null);
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onOutside();
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [onOutside]);
  return ref;
}
