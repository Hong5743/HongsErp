'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { login, ApiErrorBody } from '@/lib/api';

interface FieldErrors {
  email?: string;
  password?: string;
  general?: string;
}

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<FieldErrors>({});
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);

    try {
      await login(email, password);
      router.push('/');
    } catch (err: unknown) {
      const errObj = err as { status?: number; body?: ApiErrorBody };
      if (errObj?.body?.errors) {
        const fieldErrors: FieldErrors = {};
        for (const fe of errObj.body.errors) {
          if (fe.field === 'email') fieldErrors.email = fe.message;
          else if (fe.field === 'password') fieldErrors.password = fe.message;
          else fieldErrors.general = fe.message;
        }
        setErrors(fieldErrors);
      } else {
        setErrors({ general: '로그인에 실패했습니다. 다시 시도해주세요.' });
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
        <h1 className="text-2xl font-bold text-slate-900 mb-2">홍스 ERP</h1>
        <p className="text-sm text-slate-500 mb-8">사내 계정으로 로그인하세요</p>

        <form onSubmit={handleSubmit} noValidate className="space-y-5">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1">
              이메일
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-2 transition
                ${errors.email
                  ? 'border-red-400 focus:ring-red-200'
                  : 'border-slate-300 focus:ring-blue-200 focus:border-blue-400'
                }`}
              placeholder="name@hongs.com"
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-600">{errors.email}</p>
            )}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-1">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-2 transition
                ${errors.password
                  ? 'border-red-400 focus:ring-red-200'
                  : 'border-slate-300 focus:ring-blue-200 focus:border-blue-400'
                }`}
              placeholder="비밀번호"
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-600">{errors.password}</p>
            )}
          </div>

          {errors.general && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
              {errors.general}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-semibold rounded-lg py-2 text-sm transition"
          >
            {submitting ? '로그인 중...' : '로그인'}
          </button>
        </form>
      </div>
    </div>
  );
}
