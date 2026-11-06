import React, { useEffect, useState } from 'react';
import { ArrowRight, CreditCard, Info, ShieldCheck, Wallet } from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { parseRand, zar } from '../lib/utils';
import { Button, Field, Input, Modal, Select } from './ui';

const CHIPS = [500, 1000, 2500, 5000, 10000];

export function TopUpModal() {
  const { topUpOpen, setTopUpOpen, refresh, toast, me } = useApp();
  const [amount, setAmount] = useState('2500');
  const [method, setMethod] = useState<'payfast' | 'paystack' | 'card' | 'eft'>('payfast');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (topUpOpen) setError('');
  }, [topUpOpen]);

  const cents = parseRand(amount);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (cents < 10000) return setError('Minimum top-up is R100.');
    if (cents > 5000000) return setError('Maximum top-up is R50,000.');
    setBusy(true);
    try {
      const res = await api.topUp(cents, method);
      toast(`${zar(cents)} added to your wallet · ${res.reference}`, 'success');
      await refresh();
      setTopUpOpen(false);
    } catch (err: any) {
      setError(err?.message || 'Could not top up your wallet.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal
      open={topUpOpen}
      onClose={() => setTopUpOpen(false)}
      size="sm"
      title="Top up your wallet"
      subtitle="Funds settle instantly in this demo and can be held in escrow for a booking."
      footer={
        <Button type="submit" form="topup-form" className="w-full" loading={busy} icon={<ArrowRight className="h-4 w-4" />}>
          Add {cents >= 10000 ? zar(cents) : 'funds'}
        </Button>
      }
    >
      <form id="topup-form" onSubmit={submit} className="space-y-4">
        {error && <div className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-[12px] font-medium text-rose-700">{error}</div>}

        <div className="rounded-2xl border border-teal-900/10 bg-ink-950 p-4 text-white">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-[0.16em] text-mint-200/60">Wallet balance</span>
            <Wallet className="h-4 w-4 text-brand-400" />
          </div>
          <div className="mt-1 text-[26px] font-extrabold leading-none">{zar(me?.wallet?.balanceCents || 0, { decimals: true })}</div>
          <div className="mt-1 text-[10px] text-mint-100/60">{zar(me?.wallet?.escrowCents || 0)} held in escrow</div>
        </div>

        <div>
          <div className="mb-1.5 text-[12px] font-semibold uppercase tracking-[0.12em] text-ink-700/70">Amount</div>
          <div className="flex flex-wrap gap-1.5">
            {CHIPS.map((v) => (
              <button
                key={v}
                type="button"
                onClick={() => setAmount(String(v))}
                className={`rounded-full border px-3 py-1.5 text-[11px] font-bold transition ${cents === v * 100 ? 'border-teal-600 bg-mint-100 text-teal-900' : 'border-teal-900/10 bg-white text-slate-500 hover:border-teal-900/25'}`}
              >
                R{v.toLocaleString('en-ZA')}
              </button>
            ))}
          </div>
          <div className="relative mt-2">
            <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-[13px] font-bold text-slate-400">R</span>
            <Input type="number" min={100} max={50000} step={50} className="pl-8" value={amount} onChange={(e) => setAmount(e.target.value)} />
          </div>
        </div>

        <Field label="Payment method">
          <Select value={method} onChange={(e) => setMethod(e.target.value as any)}>
            <option value="payfast">PayFast · instant EFT</option>
            <option value="paystack">Paystack · card</option>
            <option value="card">Card on file</option>
            <option value="eft">Bank transfer (manual)</option>
          </Select>
        </Field>

        <div className="flex items-start gap-2 rounded-xl bg-mint-50 px-3 py-2 text-[10px] leading-relaxed text-teal-900">
          <ShieldCheck className="mt-0.5 h-3.5 w-3.5 shrink-0" />
          Escrow funds are held by MediGig ZA and only released to the professional after the hirer confirms completion. Declines refund automatically.
        </div>
        <div className="flex items-center gap-1.5 text-[10px] text-slate-400">
          <Info className="h-3 w-3" /> This is a product demo — no real charges are made.
        </div>
      </form>
    </Modal>
  );
}
