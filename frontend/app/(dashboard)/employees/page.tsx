'use client';

import { useState, useEffect, FormEvent } from 'react';
import type { Employee, Role } from '@/types';
import { fetchEmployees, createEmployee, type CreateEmployeePayload } from '@/lib/api';

const ROLE_BADGE: Record<Role, string> = {
  ADMIN: 'bg-red-100 text-red-700',
  EDITOR: 'bg-blue-100 text-blue-700',
  EMPLOYEE: 'bg-slate-100 text-slate-700',
};

const ROLE_LABEL: Record<Role, string> = {
  ADMIN: '관리자',
  EDITOR: '에디터',
  EMPLOYEE: '직원',
};

interface FormState {
  email: string;
  password: string;
  name: string;
  role: Role;
  department: string;
}

const EMPTY_FORM: FormState = { email: '', password: '', name: '', role: 'EMPLOYEE', department: '' };

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setEmployees(await fetchEmployees());
    } catch {
      setError('사원 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  function openModal() {
    setForm(EMPTY_FORM);
    setSubmitError(null);
    setShowModal(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setSubmitError(null);
    try {
      const payload: CreateEmployeePayload = {
        email: form.email,
        password: form.password,
        name: form.name,
        role: form.role,
        ...(form.department ? { department: form.department } : {}),
      };
      await createEmployee(payload);
    } catch {
      setSubmitError('사원 생성에 실패했습니다. 입력값을 확인해주세요.');
      setSubmitting(false);
      return;
    }
    setSubmitting(false);
    setShowModal(false);
    load();
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">사원 관리</h1>
        <button
          onClick={openModal}
          className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-lg transition"
        >
          + 사원 추가
        </button>
      </div>

      {loading && <p className="text-sm text-slate-400">불러오는 중...</p>}
      {error && <p className="text-sm text-red-600">{error}</p>}

      {!loading && !error && (
        <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-500 text-left">
              <tr>
                <th className="px-4 py-3 font-medium">이름</th>
                <th className="px-4 py-3 font-medium">이메일</th>
                <th className="px-4 py-3 font-medium">역할</th>
                <th className="px-4 py-3 font-medium">부서</th>
                <th className="px-4 py-3 font-medium">상태</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {employees.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                    등록된 사원이 없습니다.
                  </td>
                </tr>
              )}
              {employees.map((emp) => (
                <tr key={emp.id} className="hover:bg-slate-50 transition">
                  <td className="px-4 py-3 font-medium text-slate-800">{emp.name}</td>
                  <td className="px-4 py-3 text-slate-600">{emp.email}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${ROLE_BADGE[emp.role]}`}>
                      {ROLE_LABEL[emp.role]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{emp.department ?? '—'}</td>
                  <td className="px-4 py-3">
                    {emp.locked
                      ? <span className="text-xs text-red-600 font-medium">잠금</span>
                      : <span className="text-xs text-green-600 font-medium">정상</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-lg p-6 w-full max-w-md">
            <h2 className="text-lg font-bold text-slate-900 mb-5">사원 추가</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <Field label="이메일">
                <input
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  placeholder="name@hongs.com"
                  className={inputCls}
                />
              </Field>
              <Field label="비밀번호">
                <input
                  type="password"
                  required
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  placeholder="8자 이상, 대소문자·숫자·특수문자 포함"
                  className={inputCls}
                />
              </Field>
              <Field label="이름">
                <input
                  type="text"
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  className={inputCls}
                />
              </Field>
              <Field label="역할">
                <select
                  value={form.role}
                  onChange={(e) => setForm({ ...form, role: e.target.value as Role })}
                  className={inputCls}
                >
                  <option value="EMPLOYEE">직원 (EMPLOYEE)</option>
                  <option value="EDITOR">에디터 (EDITOR)</option>
                  <option value="ADMIN">관리자 (ADMIN)</option>
                </select>
              </Field>
              <Field label="부서 (선택)">
                <input
                  type="text"
                  value={form.department}
                  onChange={(e) => setForm({ ...form, department: e.target.value })}
                  placeholder="예: 개발팀"
                  className={inputCls}
                />
              </Field>

              {submitError && (
                <p className="text-sm text-red-600">{submitError}</p>
              )}

              <div className="flex gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 border border-slate-300 text-slate-700 text-sm font-medium rounded-lg py-2 hover:bg-slate-50 transition"
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-semibold rounded-lg py-2 transition"
                >
                  {submitting ? '저장 중...' : '저장'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      {children}
    </div>
  );
}

const inputCls =
  'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-blue-200 focus:border-blue-400 transition';
