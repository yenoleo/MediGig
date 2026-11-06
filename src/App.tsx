import React, { useEffect } from 'react';
import { useApp } from './lib/context';
import { Landing } from './components/Landing';
import { AuthPanel } from './components/AuthPanel';
import { Shell } from './components/Shell';
import { Dashboard } from './components/Dashboard';
import { SearchView } from './components/SearchView';
import { BookingsView } from './components/BookingsView';
import { WalletView } from './components/WalletView';
import { ProfileView } from './components/ProfileView';
import { NotificationsView } from './components/NotificationsView';
import { AICopilot } from './components/AICopilot';
import { BookingModal } from './components/BookingModal';
import { TopUpModal } from './components/TopUpModal';
import { Logo, Spinner, Toaster } from './components/ui';

function ViewRouter() {
  const { view } = useApp();
  switch (view) {
    case 'discover':
      return <SearchView />;
    case 'bookings':
      return <BookingsView />;
    case 'wallet':
      return <WalletView />;
    case 'profile':
      return <ProfileView />;
    case 'notifications':
      return <NotificationsView />;
    default:
      return <Dashboard />;
  }
}

export default function App() {
  const { loading, signedIn, toasts, dismissToast, view } = useApp();

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [view]);

  if (loading) {
    return (
      <div className="grid min-h-screen place-items-center bg-[#f4faf7]">
        <div className="flex flex-col items-center gap-4">
          <Logo />
          <div className="flex items-center gap-2 text-[12px] font-semibold text-slate-500">
            <Spinner className="h-4 w-4 text-teal-700" /> Opening your workspace…
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      {signedIn ? (
        <>
          <Shell>
            <ViewRouter />
          </Shell>
          <BookingModal />
          <TopUpModal />
          <AICopilot />
        </>
      ) : (
        <>
          <Landing />
          <AICopilot />
        </>
      )}
      <AuthPanel />
      <Toaster toasts={toasts} dismiss={dismissToast} />
    </>
  );
}
