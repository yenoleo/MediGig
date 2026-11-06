import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { motion } from 'motion/react';
import {
  ArrowUpRight, Bell, BellRing, CalendarCheck2, Check, CheckCheck, MapPin, ShieldCheck,
  Sparkles, Wallet,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { cn, relativeTime } from '../lib/utils';
import type { Notification } from '../lib/types';
import { Badge, Button, EmptyState, Segmented } from './ui';

const META: Record<string, { icon: React.ReactNode; tone: string; label: string }> = {
  request: { icon: <CalendarCheck2 className="h-4 w-4" />, tone: 'bg-amber-50 text-amber-700 border-amber-200', label: 'Request' },
  escrow: { icon: <ShieldCheck className="h-4 w-4" />, tone: 'bg-teal-50 text-teal-700 border-teal-200', label: 'Escrow' },
  accepted: { icon: <Check className="h-4 w-4" />, tone: 'bg-teal-50 text-teal-700 border-teal-200', label: 'Confirmed' },
  address: { icon: <MapPin className="h-4 w-4" />, tone: 'bg-sky-50 text-sky-700 border-sky-200', label: 'Address' },
  completion: { icon: <BellRing className="h-4 w-4" />, tone: 'bg-indigo-50 text-indigo-700 border-indigo-200', label: 'Completion' },
  payout: { icon: <Wallet className="h-4 w-4" />, tone: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'Payout' },
  wallet: { icon: <Wallet className="h-4 w-4" />, tone: 'bg-emerald-50 text-emerald-700 border-emerald-200', label: 'Wallet' },
  declined: { icon: <Bell className="h-4 w-4" />, tone: 'bg-rose-50 text-rose-700 border-rose-200', label: 'Declined' },
  cancelled: { icon: <Bell className="h-4 w-4" />, tone: 'bg-slate-100 text-slate-600 border-slate-200', label: 'Cancelled' },
  dispute: { icon: <ShieldCheck className="h-4 w-4" />, tone: 'bg-rose-50 text-rose-700 border-rose-200', label: 'Dispute' },
};

export function NotificationsView() {
  const { refresh, toast, setView, setFocusBooking, setAiOpen } = useApp();
  const [items, setItems] = useState<Notification[]>([]);
  const [unread, setUnread] = useState(0);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<'all' | 'unread'>('all');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      const res = await api.notifications();
      setItems(res.notifications);
      setUnread(res.unread);
    } catch (err: any) {
      toast(err?.message || 'Could not load notifications.', 'error');
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const shown = useMemo(() => (tab === 'unread' ? items.filter((n) => !n.read) : items), [items, tab]);
  const unreadCount = items.filter((n) => !n.read).length;

  const markAll = async () => {
    if (!unreadCount) return;
    setBusy(true);
    try {
      await api.readNotifications({ all: true });
      setItems((x) => x.map((n) => ({ ...n, read: true })));
      setUnread(0);
      await refresh();
      toast('All notifications marked as read.', 'success');
    } catch (err: any) {
      toast(err?.message || 'Could not update notifications.', 'error');
    } finally {
      setBusy(false);
    }
  };

  const open = async (n: Notification) => {
    if (!n.read) {
      try {
        await api.readNotifications({ ids: [n.id] });
        setItems((x) => x.map((y) => (y.id === n.id ? { ...y, read: true } : y)));
        setUnread((u) => Math.max(0, u - 1));
        refresh();
      } catch {}
    }
    if (n.bookingId) {
      setFocusBooking(n.bookingId);
      setView('bookings');
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <div className="text-[10px] font-bold uppercase tracking-[0.18em] text-brand-700/70">Inbox</div>
          <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-ink-950 sm:text-3xl">Notifications</h1>
          <p className="mt-1 max-w-xl text-[12px] text-slate-600">
            {unread ? `${unread} unread — shift requests, escrow movements and payout confirmations.` : 'Everything is read. New escrow and booking activity will land here.'}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Segmented
            value={tab}
            onChange={setTab}
            options={[
              { value: 'all', label: `All (${items.length})` },
              { value: 'unread', label: `Unread (${unreadCount})` },
            ]}
          />
          <Button size="sm" variant="outline" onClick={markAll} loading={busy} disabled={!unreadCount} icon={<CheckCheck className="h-3.5 w-3.5" />}>
            Mark read
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="space-y-2">{Array.from({ length: 4 }).map((_, i) => <div key={i} className="shimmer h-20 rounded-2xl" />)}</div>
      ) : shown.length === 0 ? (
        <EmptyState
          icon={<Bell className="h-5 w-5" />}
          title={tab === 'unread' ? 'No unread notifications' : 'Nothing here yet'}
          body={tab === 'unread' ? 'You are all caught up. Switch to All to see your history.' : 'Book a professional or post a shift — every escrow movement will be recorded here.'}
        />
      ) : (
        <div className="space-y-2">
          {shown.map((n, i) => {
            const meta = META[n.type] || { icon: <Bell className="h-4 w-4" />, tone: 'bg-slate-100 text-slate-600 border-slate-200', label: n.type };
            return (
              <motion.button
                key={n.id}
                layout
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: Math.min(i * 0.03, 0.2) }}
                onClick={() => open(n)}
                className={cn(
                  'w-full rounded-2xl border p-3.5 text-left transition',
                  n.read ? 'border-teal-900/8 bg-white/70 hover:border-teal-900/20' : 'border-teal-600/25 bg-mint-50/70 hover:border-teal-600/40',
                )}
              >
                <div className="flex items-start gap-3">
                  <span className={cn('grid h-9 w-9 shrink-0 place-items-center rounded-xl border', meta.tone)}>{meta.icon}</span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-1.5">
                      <span className={cn('text-[13px] font-bold text-ink-950', !n.read && 'text-teal-900')}>{n.title}</span>
                      {!n.read && <span className="h-1.5 w-1.5 rounded-full bg-teal-600" />}
                      <span className="ml-auto text-[9px] font-medium uppercase tracking-wider text-slate-400">{relativeTime(n.createdAt)}</span>
                    </div>
                    <p className="mt-0.5 text-[11px] leading-relaxed text-slate-600">{n.body}</p>
                    <div className="mt-1.5 flex items-center gap-2">
                      <Badge tone="neutral" className="!py-0 text-[9px]">{meta.label}</Badge>
                      {n.bookingId && (
                        <span className="inline-flex items-center gap-0.5 text-[9px] font-bold text-teal-700">
                          View booking <ArrowUpRight className="h-2.5 w-2.5" />
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </motion.button>
            );
          })}
        </div>
      )}

      <section className="card flex flex-wrap items-center justify-between gap-3 p-4">
        <div className="flex items-center gap-2">
          <span className="grid h-8 w-8 place-items-center rounded-xl bg-mint-100 text-teal-700"><Sparkles className="h-4 w-4" /></span>
          <div>
            <div className="text-[12px] font-bold text-ink-950">Need an answer faster?</div>
            <div className="text-[10px] text-slate-500">The copilot knows your escrow, bookings and payouts.</div>
          </div>
        </div>
        <Button size="xs" variant="primary" onClick={() => setAiOpen(true)} icon={<Sparkles className="h-3 w-3 text-brand-400" />}>Ask MediGig AI</Button>
      </section>
    </div>
  );
}
