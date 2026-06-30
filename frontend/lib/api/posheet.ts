import type {
  PoFolder, PoFile, PresignedUrlResponse, PermissionResponse,
  TrashItem, VersionHistory, PoSetting,
} from '@/types/posheet';

const BASE = '/api';

async function req<T>(input: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + input, { credentials: 'include', ...init });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message ?? res.statusText);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// ---- 폴더 ----
export const listFolders = (parentId?: number) =>
  req<PoFolder[]>(`/posheet/folders${parentId != null ? `?parentId=${parentId}` : ''}`);

export const createFolder = (name: string, parentId?: number) =>
  req<PoFolder>('/posheet/folders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, parentId: parentId ?? null }),
  });

export const deleteFolder = (id: number) =>
  req<void>(`/posheet/folders/${id}`, { method: 'DELETE' });

// ---- 파일 ----
export const listFiles = (folderId: number) =>
  req<PoFile[]>(`/posheet/folders/${folderId}/files`);

export const uploadFile = (folderId: number, file: File) => {
  const fd = new FormData();
  fd.append('file', file);
  return req<PoFile>(`/posheet/folders/${folderId}/files`, { method: 'POST', body: fd });
};

export const replaceFile = (fileId: number, file: File) => {
  const fd = new FormData();
  fd.append('file', file);
  return req<PoFile>(`/posheet/files/${fileId}`, { method: 'PUT', body: fd });
};

export const getPreviewUrl = (fileId: number) =>
  req<PresignedUrlResponse>(`/posheet/files/${fileId}/preview`);

export const deleteFile = (fileId: number) =>
  req<void>(`/posheet/files/${fileId}`, { method: 'DELETE' });

// ---- Admin: 권한 ----
export const listPermissions = () =>
  req<PermissionResponse[]>('/admin/posheet/permissions');

export const grantPermission = (empId: number) =>
  req<void>(`/admin/posheet/permissions/${empId}/grant`, { method: 'POST' });

export const revokePermission = (empId: number) =>
  req<void>(`/admin/posheet/permissions/${empId}/revoke`, { method: 'DELETE' });

// ---- Admin: 휴지통 ----
export const listTrash = () => req<TrashItem[]>('/admin/posheet/trash');

export const purgeTrash = () =>
  req<{ purged: number }>('/admin/posheet/trash/purge', { method: 'DELETE' });

// ---- Admin: 버전 이력 ----
export const listVersionHistory = () =>
  req<VersionHistory[]>('/admin/posheet/versions');

// ---- Admin: 설정 ----
export const listSettings = () => req<PoSetting[]>('/admin/posheet/settings');

export const updateSetting = (key: string, value: string) =>
  req<PoSetting>(`/admin/posheet/settings/${encodeURIComponent(key)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ value }),
  });
