import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import {
  ArrowUp, BadgeCheck, CalendarDays, MapPin, RefreshCw, SendHorizonal, Sparkles, Star, X,
  Compass, ShieldCheck, Wallet,
} from 'lucide-react';
import { api } from '../lib/api';
import { useApp } from '../lib/context';
import { cn, zar } from '../lib/utils';
import type { AiMatch, ChatMessage, ProfessionalCard } from '../lib/types';
import { Avatar, Badge, Button, Drawer, IconButton } from './ui';
import { ThinkingState, ToolCallRow, useTypewriter } from './effects';

const STEP_LABELS = [
  { label: 'search_marketplace', detail: 'verified professionals' },
  { label: 'read_wallet_context', detail: 'escrow + balance' },
  { label: 'rank_matches', detail: 'distance · rate · rating' },
  { label: 'compose_reply', detail: 'writing answer' },
];

let seq = 0;
const nextId = () => `msg_${Date.now()}_${seq++}`;

export function AICopilot() {
  const { aiOpen, setAiOpen, me, mode, setHireTarget, toast } = useApp();
  const user = me?.user;

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [status, setStatus] = useState<{ configured: boolean; provider: string; model: string } | null>(null);
  const [loadingMatch, setLoadingMatch] = useState<string | null>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  const greeting = useMemo(() => {
    if (mode === 'hire') {
      return `Hi${user ? ` ${user.name.replace(/^(Dr\.?|Prof\.?)\s+/i, '').split(' ')[0]}` : ''}. I can find cover, check which professionals are free, and explain how escrow protects the practice. What shift do you need?`;
    }
    return `Hi${user ? ` ${user.name.replace(/^(Dr\.?|Prof\.?)\s+/i, '').split(' ')[0]}` : ''}. Ask me which shifts suit your certifications, when your next payout lands, or how to raise your day rate without losing bookings.`;
  }, [mode, user?.id]);

  useEffect(() => {
    api.aiStatus().then((s) => setStatus({ configured: s.configured, provider: s.provider, model: s.model })).catch(() => {});
  }, []);

  useEffect(() => {
    if (!aiOpen) return;
    if (messages.length === 0) {
      setMessages([{ id: nextId(), role: 'assistant', text: greeting }]);
    }
    setTimeout(() => inputRef.current?.focus(), 260);
  }, [aiOpen, greeting, messages.length]);

  useEffect(() => {
    if (!aiOpen) return;
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') setAiOpen(false); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [aiOpen, setAiOpen]);

  useEffect(() => {
    const el = scrollRef.current;
    if (el) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
  }, [messages, busy]);

  const send = useCallback(async (raw?: string) => {
    const text = (raw ?? input).trim();
    if (!text || busy) return;
    setInput('');
    const history = messages.filter((m) => !m.pending).map((m) => ({ role: m.role, text: m.text }));
    const pendingId = nextId();
    setMessages((m) => [
      ...m,
      { id: nextId(), role: 'user', text },
      { id: pendingId, role: 'assistant', text: '', pending: true },
    ]);
    setBusy(true);
    try {
      const res = await api.copilot(text, history.slice(-8));
      setMessages((m) =>
        m.map((x) =>
          x.id === pendingId
            ? {
                ...x,
                pending: false,
                text: res.reply,
                matches: res.matches,
                meta: { provider: res.provider, model: res.model, latencyMs: res.latencyMs },
              }
            : x,
        ),
      );
      setSuggestions(res.suggestions || []);
    } catch (err: any) {
      setMessages((m) => m.map((x) => (x.id === pendingId ? { ...x, pending: false, text: err?.message || 'The copilot could not answer that just now. Try again in a moment.' } : x)));
      toast(err?.message || 'The copilot is unavailable right now.', 'error');
    } finally {
      setBusy(false);
    }
  }, [busy, input, messages, toast]);

  const reset = () => {
    setMessages([{ id: nextId(), role: 'assistant', text: greeting }]);
    setSuggestions([]);
    setInput('');
  };

  const pickMatch = async (m: AiMatch) => {
    setLoadingMatch(m.userId);
    try {
      const { professional } = await api.professional(m.userId);
      setHireTarget(professional as ProfessionalCard);
      setAiOpen(false);
    } catch (err: any) {
      toast(err?.message || 'Could not open that profile.', 'error');
    } finally {
      setLoadingMatch(null);
    }
  };

  const chips = suggestions.length
    ? suggestions
    : mode === 'hire'
      ? ['Find an ICU nurse near me', 'Draft a shift brief for a Saturday clinic', 'How does escrow protect me?']
      : ['Find cardiology shifts in Johannesburg', 'When do I get paid?', 'Summarise my earnings'];

  return (
    <Drawer open={aiOpen} onClose={() => setAiOpen(false)} className="sm:max-w-[520px]">
      <div className="relative flex h-full flex-col bg-[#f7fcfa]">
        {/* header */}
        <div className="relative overflow-hidden border-b border-teal-900/8 bg-ink-950 px-4 py-3.5 text-white">
          <div className="pointer-events-none absolute -right-10 -top-14 h-40 w-40 rounded-full bg-brand-500/25 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-16 left-10 h-32 w-32 rounded-full bg-amber-400/10 blur-3xl" />
          <div className="relative flex items-center gap-2.5">
            <span className="relative grid h-9 w-9 place-items-center rounded-xl bg-brand-500/20 text-brand-400">
              <Sparkles className="h-4 w-4" />
              <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-emerald-400 ring-2 ring-ink-950" />
            </span>
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-1.5 text-[13px] font-bold">MediGig Copilot</div>
              <div className="truncate text-[10px] text-mint-100/55">
                {status ? `${status.provider} · ${status.model}` : 'Reading your workspace…'}
              </div>
            </div>
            <button onClick={reset} className="grid h-8 w-8 place-items-center rounded-full text-mint-100/50 transition hover:bg-white/10 hover:text-white" aria-label="New conversation">
              <RefreshCw className="h-3.5 w-3.5" />
            </button>
            <IconButton onClick={() => setAiOpen(false)} className="!h-8 !w-8 !border-white/10 !bg-white/5 !text-white" aria-label="Close copilot">
              <X className="h-3.5 w-3.5" />
            </IconButton>
          </div>
        </div>

        {/* transcript */}
        <div ref={scrollRef} className="hide-scrollbar flex-1 space-y-3 overflow-y-auto px-4 py-4">
          {messages.map((m) =>
            m.role === 'user' ? (
              <motion.div key={m.id} initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} className="flex justify-end">
                <div className="max-w-[85%] rounded-2xl rounded-br-md bg-ink-900 px-3.5 py-2.5 text-[12px] leading-relaxed text-white shadow-sm">
                  {m.text}
                </div>
              </motion.div>
            ) : (
              <motion.div key={m.id} initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} className="flex gap-2">
                <span className="mt-0.5 grid h-6 w-6 shrink-0 place-items-center rounded-lg bg-mint-100 text-teal-700">
                  <Sparkles className="h-3 w-3" />
                </span>
                <div className="min-w-0 flex-1 space-y-2">
                  {m.pending ? (
                    <>
                      <ThinkingState active phases={['Reading your workspace', 'Searching the marketplace', 'Checking escrow context', 'Writing a reply']} />
                      <div className="space-y-1.5">
                        {STEP_LABELS.slice(0, 3).map((s, i) => (
                          <ToolCallRow key={s.label} label={s.label} detail={s.detail} status={i === 0 ? 'running' : 'done'} index={i} />
                        ))}
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="rounded-2xl rounded-tl-md border border-teal-900/8 bg-white px-3.5 py-2.5 text-[12px] leading-relaxed text-ink-900 shadow-sm">
                        <AiText text={m.text} />
                      </div>
                      {m.meta && (
                        <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-[9px] text-slate-400">
                          <span className="font-mono">{m.meta.provider}/{m.meta.model}</span>
                          {typeof m.meta.latencyMs === 'number' && <span>· {(m.meta.latencyMs / 1000).toFixed(1)}s</span>}
                          <span className="ml-auto inline-flex items-center gap-0.5 text-emerald-600"><ShieldCheck className="h-2.5 w-2.5" /> escrow aware</span>
                        </div>
                      )}

                      {!!m.matches?.length && (
                        <div className="space-y-2">
                          <div className="text-[9px] font-bold uppercase tracking-[0.14em] text-slate-400">Suggested professionals</div>
                          {m.matches.map((match) => (
                            <MatchCard
                              key={match.userId}
                              match={match}
                              loading={loadingMatch === match.userId}
                              onOpen={() => pickMatch(match)}
                            />
                          ))}
                        </div>
                      )}
                    </>
                  )}
                </div>
              </motion.div>
            ),
          )}
        </div>

        {/* suggestions */}
        <div className="border-t border-teal-900/8 px-4 pt-2.5">
          <div className="hide-scrollbar flex gap-1.5 overflow-x-auto pb-2">
            {chips.map((c) => (
              <button
                key={c}
                onClick={() => send(c)}
                disabled={busy}
                className="shrink-0 rounded-full border border-teal-900/10 bg-white px-2.5 py-1 text-[10px] font-semibold text-slate-600 transition hover:border-teal-600/40 hover:text-teal-800 disabled:opacity-50"
              >
                {c}
              </button>
            ))}
          </div>
        </div>

        {/* composer */}
        <div className="border-t border-teal-900/8 bg-white/80 px-3 py-3">
          <div className="rounded-2xl border border-teal-900/12 bg-white p-1.5 shadow-[0_18px_40px_-32px_rgba(4,33,29,.6)] transition focus-within:border-teal-600/50">
            <textarea
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
              }}
              rows={2}
              placeholder={mode === 'hire' ? 'e.g. Find an ICU nurse in Sandton tomorrow under R1,200' : 'e.g. Which of my available days have the best shifts?'}
              className="hide-scrollbar max-h-28 w-full resize-none bg-transparent px-2.5 py-1.5 text-[12px] leading-relaxed text-ink-900 outline-none placeholder:text-slate-400"
            />
            <div className="flex items-center justify-between px-1.5 pb-0.5">
              <span className="text-[9px] text-slate-400">Enter to send · Shift+Enter for a new line</span>
              <Button
                size="xs"
                variant="brand"
                onClick={() => send()}
                disabled={!input.trim() || busy}
                loading={busy}
                icon={<ArrowUp className="h-3 w-3" />}
                className="!px-2.5"
              >
                Send
              </Button>
            </div>
          </div>
          <div className="mt-1.5 flex items-center justify-center gap-1 text-[9px] text-slate-400">
            <ShieldCheck className="h-2.5 w-2.5 text-teal-600" /> MediGig copilot never shares patient data or full addresses.
          </div>
        </div>
      </div>
    </Drawer>
  );
}

function AiText({ text }: { text: string }) {
  const shown = useTypewriter(text, 8);
  return <span className="whitespace-pre-wrap">{shown}{shown.length < text.length && <span className="caret" />}</span>;
}

function MatchCard({ match, onOpen, loading }: { match: AiMatch; onOpen: () => void; loading: boolean }) {
  return (
    <motion.button
      initial={{ opacity: 0, y: 5 }}
      animate={{ opacity: 1, y: 0 }}
      onClick={onOpen}
      disabled={loading}
      className="flex w-full items-center gap-2.5 rounded-xl border border-teal-900/10 bg-white px-2.5 py-2 text-left transition hover:-translate-y-0.5 hover:border-teal-600/40 hover:shadow-md disabled:opacity-60"
    >
      <Avatar name={match.name} size="sm" />
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1">
          <span className="truncate text-[11px] font-bold text-ink-950">{match.name}</span>
          {match.verified && <BadgeCheck className="h-3 w-3 shrink-0 text-teal-600" />}
          {match.rating > 0 && (
            <span className="ml-auto inline-flex shrink-0 items-center gap-0.5 text-[9px] font-semibold text-amber-700">
              <Star className="h-2.5 w-2.5 fill-amber-400 text-amber-400" /> {match.rating.toFixed(1)}
            </span>
          )}
        </div>
        <div className="truncate text-[9px] text-slate-500">{match.profession}{match.specialty ? ` · ${match.specialty}` : ''}</div>
        <div className="mt-0.5 flex items-center gap-2 text-[9px] text-slate-400">
          <span className="inline-flex items-center gap-0.5"><MapPin className="h-2.5 w-2.5" />{match.distanceKm} km</span>
          <span className="inline-flex items-center gap-0.5"><Wallet className="h-2.5 w-2.5" />{zar(match.dailyRateCents)}/day</span>
        </div>
      </div>
      <span className="shrink-0 rounded-full bg-mint-100 px-2 py-1 text-[9px] font-bold text-teal-800">{loading ? '…' : 'View'}</span>
    </motion.button>
  );
}
