'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const navItems = [
  { href: '/', label: '대시보드', icon: '📊' },
  { href: '/orders', label: '오더 관리', icon: '📦' },
  { href: '/employees', label: '사원 관리', icon: '👤' },
  { href: '/partners', label: '거래처 관리', icon: '🏢' },
  { href: '/accessories', label: '부자재 관리', icon: '🔩' },
  { href: '/settlement', label: '정산 관리', icon: '💰' },
  { href: '/leave', label: '연차 관리', icon: '📅' },
  { href: '/notifications', label: '알림', icon: '🔔' },
  { href: '/posheet', label: 'PO Sheet', icon: '📋' },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  const pathname = usePathname();

  return (
    <>
      {/* 모바일 오버레이 */}
      {open && (
        <div
          className="fixed inset-0 bg-black/40 z-20 lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* 사이드바 */}
      <aside
        className={[
          'fixed top-0 left-0 h-full w-64 bg-slate-900 text-white z-30 flex flex-col transition-transform duration-300',
          'lg:static lg:translate-x-0 lg:z-auto',
          open ? 'translate-x-0' : '-translate-x-full',
        ].join(' ')}
      >
        {/* 로고 */}
        <div className="flex items-center justify-between h-16 px-6 border-b border-slate-700 shrink-0">
          <span className="text-xl font-bold tracking-tight">홍스 ERP</span>
          <button
            className="lg:hidden text-slate-400 hover:text-white"
            onClick={onClose}
            aria-label="사이드바 닫기"
          >
            ✕
          </button>
        </div>

        {/* 네비게이션 */}
        <nav className="flex-1 overflow-y-auto py-4">
          <ul className="space-y-1 px-3">
            {navItems.map(({ href, label, icon }) => {
              const isActive = pathname === href;
              return (
                <li key={href}>
                  <Link
                    href={href}
                    onClick={onClose}
                    className={[
                      'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-slate-700 text-white'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-white',
                    ].join(' ')}
                  >
                    <span className="text-base">{icon}</span>
                    {label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>
    </>
  );
}
