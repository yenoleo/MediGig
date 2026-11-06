import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ArrowDownLeft, ArrowUpRight, Banknote, Building2, CreditCard, Download, Landmark,
  Plus, ShieldCheck, Wallet,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { cn, fmtDate, fmtTime, parseRand, relativeTime, zar } from '../lib/utils';
import type { PaymentMethod, Transaction, Wallet as WalletType } from '../lib/types';
import { Badge, Button, EmptyState, Field, Input, Modal, SectionTitle, Select, StatCard } from './ui';
import { EarningsChart, NumberTicker } from './effects';

const TX_META: Record<string, { label: string; tone: string; icon: React.ReactNode }> = {
  topup: { label: 'Top-up', tone: 'bg-emerald-50 text-emerald-700', icon: <ArrowDownLeft className="h-3.5 w-3.5" /> },
  escrow_hold: { label: 'Escrow hold', tone: 'bg-amber-50 text-amber-700', icon: <ShieldCheck className="h-3.5 w-3.5" /> },
  escrow_release: { label: 'Escrow release', tone: 'bg-emerald-50 text-emerald-700', icon: <ArrowDownLeft className="h-3.5 w-3.5" /> },
  escrow_refund: { label: 'Refund', tone: 'bg-sky-50 text-sky-700', icon: <ArrowDownLeft className="h-3.5 w-3.5" /> },
  withdrawal: { label: 'Withdrawal', tone: 'bg-violet-50 text-violet-700', icon: <ArrowUpRight className="h-3.5 w-3.5" /> },
};

export function WalletView() {
  const { me, refresh, toast, setTopUpOpen } = useApp();
  const [wallet, setWallet] = useState<WalletType | null>(null);
  const [txns, setTxns] = useState<Transaction[]>([]);
  const [methods, setMethods] = useState<PaymentMethod[]>([]);
  const [earnings, setEarnings] = useState<Record<string, number>>({});
  const [filter, setFilter] = useState('all');
  const [withdrawOpen, setWithdrawOpen] = useState(false);
  const [methodOpen, setMethodOpen] = useState(false);
  const [amount, setAmount] = useState('1000');
  const [bank, setBank] = useState('FNB •••• 7781');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [newMethod, setNewMethod] = useState({ type: 'card', label: '' });

  const load = useCallback(async () => {
    try {
      const res = await api.wallet();
      setWallet(res.wallet);
      setTxns(res.transactions);
      setMethods(res.paymentMethods);
      setEarnings(res.earningsByMonth);
    } catch (err: any) {
      toast(err?.message || 'Could not load your wallet.', 'error');
    }
  }, [toast]);

  useEffect(() => { load(); }, [load]);

  const chart = useMemo(() => {
    const keys: string[] = [];
    const d = new Date();
    for (let i = 5; i >= 0; i--) {
      const x = new Date(d.getFullYear(), d.getMonth() - i, 1);
      keys.push(x.toISOString().slice(0, 7));
    }
    return keys.map((k) => ({
      label: new Date(`${k}-01T12:00:00`).toLocaleDateString('en-ZA', { month: 'short' }),
      value: Math.round((earnings[k] || 0) / 100),
    }));
  }, [earnings]);

  const earnedTotal = Object.values(earnings).reduce((s, v) => s + v, 0);
  const shown = txns.filter((t) => filter === 'all' || t.type === filter);

  async function withdraw(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    const cents = parseRand(amount);
    if (cents < 10000) return setError('Minimum withdrawal is R100.');
    if (cents > (wallet?.balanceCents || 0)) return setError('Withdrawal is more than your available balance.');
    setBusy(true);
    try {
      await api.withdraw(cents, bank);
      toast(`${zar(cents)} is on its way to ${bank}.`, 'success');
      setWithdrawOpen(false);
      await load();
      await refresh();
    } catch (err: any) {
      setError(err?.message || 'Could not process the withdrawal.');
    } finally {
      setBusy(false);
    }
  }

  async function addMethod(e: React.FormEvent) {
    e.preventDefault();
    if (!newMethod.label.trim()) return setError('Add a label, e.g. FNB •••• 7781.');
    setBusy(true);
    try {
      const res = await api.addPaymentMethod(newMethod);
      setMethods(res.paymentMethods);
      setMethodOpen(false);
      setNewMethod({ type: 'card', label: '' });
      toast('Payment method added.', 'success');
    } catch (err: any) {
      setError(err?.message || 'Could not add the payment method.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <div className="text-[10px] font-bold uppercase tracking-[0.18em] text-brand-700/70">Wallet</div>
          <h1 className="mt-1 text-2xl font-extrabold tracking-tight text-ink-950 sm:text-3xl">Money, held safely.</h1>
          <p className="mt-1 max-w-xl text-[12px] text-slate-600">Top up to fund bookings, or withdraw released payouts. Escrow is tracked separately from available balance.</p>
        </div>
        <div className="flex gap-2">
          <Button size="sm" variant="outline" onClick={() => setTopUpOpen(true)} icon={<Plus className="h-3.5 w-3.5" />}>Top up</Button>
          <Button size="sm" onClick={() => { setAmount('1000'); setError(''); setWithdrawOpen(true); }} icon={<ArrowUpRight className="h-3.5 w-3.5" />}>Withdraw</Button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          label="Available"
          value={<NumberTicker value={(wallet?.balanceCents || 0) / 100} format={(n) => zar(Math.round(n) * 100, { decimals: true })} />}
          sub="Ready to spend or withdraw"
          icon={<Wallet className="h-4 w-4" />}
          tone="dark"
        />
        <StatCard label="In escrow" value={zar(wallet?.escrowCents || 0)} sub="Held against active bookings" icon={<ShieldCheck className="h-4 w-4" />} tone="mint" />
        <StatCard label={me?.freelancer ? 'Released to you' : 'Total funded'} value={zar(me?.freelancer ? earnedTotal : (wallet?.balanceCents || 0) + (wallet?.escrowHeldCents || 0) - (wallet?.escrowPendingCents || 0))} sub={me?.freelancer ? 'All-time escrow releases' : 'Top-ups minus refunds'} icon={<Banknote className="h-4 w-4" />} tone="sand" />
        <StatCard label="Payout schedule" value="5 days" sub="Business days after confirmation" icon={<Landmark className="h-4 w-4" />} tone="white" />
      </div>

      <div className="grid gap-5 lg:grid-cols-[1.5fr_1fr]">
        <section className="card p-4 sm:p-5">
          <SectionTitle
            eyebrow={me?.freelancer ? 'Earnings' : 'Wallet activity'}
            title={me?.freelancer ? 'Escrow releases' : 'Top-ups and escrow'}
            sub="Last 6 months, in ZAR"
            action={<Badge tone="emerald">No payout fees</Badge>}
          />
          <EarningsChart data={chart} height={160} format={(n) => zar(n * 100)} />
          <div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-3">
            {[
              { k: 'This month', v: zar(chart[chart.length - 1]?.value * 100 || 0) },
              { k: 'Last month', v: zar(chart[chart.length - 2]?.value * 100 || 0) },
              { k: '6-month total', v: zar(chart.reduce((s, c) => s + c.value, 0) * 100) },
            ].map((x) => (
              <div key={x.k} className="rounded-xl border border-teal-900/8 bg-white/70 px-3 py-2">
                <div className="text-[9px] font-bold uppercase tracking-wider text-slate-400">{x.k}</div>
                <div className="mt-0.5 text-[13px] font-extrabold text-ink-950">{x.v}</div>
              </div>
            ))}
          </div>
        </section>

        <div className="space-y-5">
          <section className="relative overflow-hidden rounded-[24px] bg-ink-950 p-4 text-white sm:p-5">
            <div className="pointer-events-none absolute -right-12 -top-12 h-40 w-40 rounded-full bg-brand-500/20 blur-3xl" />
            <div className="relative">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-bold uppercase tracking-[0.16em] text-mint-200/60">Available to withdraw</span>
                <Wallet className="h-4 w-4 text-brand-400" />
              </div>
              <div className="mt-1 text-[28px] font-extrabold leading-none">{zar(wallet?.balanceCents || 0, { decimals: true })}</div>
              <div className="mt-1.5 text-[10px] text-mint-100/60">{zar(wallet?.escrowCents || 0)} still held in escrow</div>
              <div className="mt-4 flex gap-2">
                <Button size="sm" variant="white" className="flex-1" onClick={() => setTopUpOpen(true)} icon={<Plus className="h-3.5 w-3.5 text-teal-700" />}>Top up</Button>
                <Button size="sm" className="flex-1 bg-brand-500 hover:bg-brand-600" onClick={() => { setAmount('1000'); setError(''); setWithdrawOpen(true); }}>Withdraw</Button>
              </div>
            </div>
          </section>

          <section className="card p-4 sm:p-5">
            <SectionTitle
              eyebrow="Payment methods"
              title="Cards & accounts"
              action={<button onClick={() => { setError(''); setMethodOpen(true); }} className="text-[11px] font-bold text-teal-700 hover:underline">Add</button>}
            />
            <div className="space-y-2">
              {methods.length === 0 && <div className="rounded-xl border border-dashed border-teal-900/15 p-3 text-center text-[11px] text-slate-400">No payment methods yet.</div>}
              {methods.map((m) => (
                <div key={m.id} className="flex items-center gap-2.5 rounded-xl border border-teal-900/8 bg-white px-3 py-2">
                  <span className="grid h-7 w-7 place-items-center rounded-lg bg-mint-100 text-teal-700">
                    {m.type === 'bank' ? <Building2 className="h-3.5 w-3.5" /> : <CreditCard className="h-3.5 w-3.5" />}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[11px] font-semibold text-ink-900">{m.label}</div>
                    <div className="text-[9px] text-slate-400">{m.type === 'bank' ? 'Bank account' : 'Card'}{m.expiry ? ` · expires ${m.expiry}` : ''}</div>
                  </div>
                  {m.primary && <Badge tone="teal">Primary</Badge>}
                </div>
              ))}
            </div>
            <div className="mt-3 flex items-start gap-2 rounded-xl bg-mint-50 px-3 py-2 text-[10px] leading-relaxed text-teal-900">
              <ShieldCheck className="mt-0.5 h-3.5 w-3.5 shrink-0" /> MediGig never stores raw card numbers. Top-ups are processed by PayFast or Paystack.
            </div>
          </section>
        </div>
      </div>

      <section className="card p-4 sm:p-5">
        <SectionTitle
          eyebrow="Ledger"
          title="Transactions"
          action={
            <Select value={filter} onChange={(e) => setFilter(e.target.value)} className="h-8 w-auto text-[11px]">
              <option value="all">All types</option>
              <option value="topup">Top-ups</option>
              <option value="escrow_hold">Escrow holds</option>
              <option value="escrow_release">Releases</option>
              <option value="withdrawal">Withdrawals</option>
            </Select>
          }
        />
        {shown.length === 0 ? (
          <EmptyState icon={<Banknote className="h-5 w-5" />} title="No transactions" body="Top up your wallet or complete a shift to see activity here." />
        ) : (
          <div className="divide-y divide-teal-900/6">
            {shown.map((t) => {
              const meta = TX_META[t.type] || { label: t.type, tone: 'bg-slate-100 text-slate-500', icon: <Banknote className="h-3.5 w-3.5" /> };
              return (
                <div key={t.id} className="flex items-center gap-3 py-2.5">
                  <span className={cn('grid h-8 w-8 shrink-0 place-items-center rounded-xl', meta.tone)}>{meta.icon}</span>
                  <div className="min-w-0 flex-1">
                    <div className="truncate text-[12px] font-semibold text-ink-900">{t.description}</div>
                    <div className="text-[9px] text-slate-400">{fmtDate(t.createdAt)} · {fmtTime(t.createdAt)} · {t.ref || t.id}{t.status === 'processing' ? ' · processing' : ''}</div>
                  </div>
                  <div className="text-right">
                    <div className={cn('text-[12px] font-bold', t.amountCents < 0 ? 'text-ink-900' : 'text-emerald-700')}>{t.amountCents < 0 ? '−' : '+'}{zar(Math.abs(t.amountCents))}</div>
                    <div className="text-[9px] text-slate-400">{meta.label}</div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {/* withdraw */}
      <Modal
        open={withdrawOpen}
        onClose={() => setWithdrawOpen(false)}
        size="sm"
        title="Withdraw to your bank"
        subtitle="1–2 business days · no withdrawal fee"
        footer={<Button type="submit" form="withdraw-form" className="w-full" loading={busy} icon={<ArrowUpRight className="h-4 w-4" />}>Withdraw {amount ? zar(parseRand(amount)) : ''}</Button>}
      >
        <form id="withdraw-form" onSubmit={withdraw} className="space-y-4">
          {error && <div className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>}
          <div className="rounded-2xl border border-teal-900/10 bg-mint-50/70 p-3">
            <div className="text-[10px] font-bold uppercase tracking-wider text-teal-700/60">Available</div>
            <div className="mt-0.5 text-[20px] font-extrabold text-ink-950">{zar(wallet?.balanceCents || 0, { decimals: true })}</div>
          </div>
          <Field label="Amount (ZAR)">
            <Input type="number" min={100} step={50} value={amount} onChange={(e) => setAmount(e.target.value)} />
          </Field>
          <Field label="Bank account">
            <Select value={bank} onChange={(e) => setBank(e.target.value)}>
              <option>FNB •••• 7781</option>
              <option>Capitec •••• 1251</option>
              <option>Standard Bank •••• 4402</option>
              <option>Nedbank •••• 9987</option>
            </Select>
          </Field>
          <p className="text-[10px] leading-relaxed text-slate-400">Withdrawals are only available on released escrow. Funds still held for an active booking cannot be withdrawn.</p>
        </form>
      </Modal>

      {/* add method */}
      <Modal
        open={methodOpen}
        onClose={() => setMethodOpen(false)}
        size="sm"
        title="Add a payment method"
        subtitle="Used for top-ups and payouts in this demo."
        footer={<Button type="submit" form="method-form" className="w-full" loading={busy} icon={<Plus className="h-4 w-4" />}>Add method</Button>}
      >
        <form id="method-form" onSubmit={addMethod} className="space-y-4">
          {error && <div className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>}
          <Field label="Type">
            <Select value={newMethod.type} onChange={(e) => setNewMethod({ ...newMethod, type: e.target.value })}>
              <option value="card">Card</option>
              <option value="bank">Bank account</option>
            </Select>
          </Field>
          <Field label="Label">
            <Input placeholder={newMethod.type === 'bank' ? 'FNB •••• 7781' : 'Visa •••• 4242'} value={newMethod.label} onChange={(e) => setNewMethod({ ...newMethod, label: e.target.value })} />
          </Field>
          <p className="text-[10px] text-slate-400">Demo only — no card details are collected or stored.</p>
        </form>
      </Modal>
    </div>
  );
}
