import type { Employee, Role } from '@/types';

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080/api';

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!res.ok) throw new Error(`API error ${res.status}: ${path}`);
  return res.json() as Promise<T>;
}

export async function login(email: string, password: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw { status: res.status, body };
  }
}

export async function logout(): Promise<void> {
  await fetch(`${BASE_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  errors: ApiFieldError[];
}

export interface CreateEmployeePayload {
  email: string;
  password: string;
  name: string;
  role: Role;
  department?: string;
}

export async function fetchEmployees(): Promise<Employee[]> {
  return apiFetch<Employee[]>('/admin/employees');
}

export async function createEmployee(payload: CreateEmployeePayload): Promise<Employee> {
  return apiFetch<Employee>('/admin/employees', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
