export type Role = 'ADMIN' | 'EMPLOYEE' | 'EDITOR';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface Employee {
  id: number;
  email: string;
  name: string;
  role: Role;
  locked: boolean;
  department: string | null;
}
