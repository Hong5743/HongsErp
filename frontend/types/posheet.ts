export interface PoFolder {
  id: number;
  name: string;
  parentId: number | null;
  path: string;
  createdAt: string;
}

export interface PoFile {
  id: number;
  folderId: number;
  name: string;
  currentVersion: number;
  fileSize: number;
  createdAt: string;
}

export interface PresignedUrlResponse {
  url: string;
  expiresIn: string;
}

export interface PermissionResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  grantedBy: number;
  grantedAt: string;
  active: boolean;
  revokedAt: string | null;
}

export interface TrashItem {
  type: 'FILE' | 'FOLDER';
  id: number;
  name: string;
  deletedAt: string;
  purgeAt: string;
  deleteBatchId: string;
}

export interface VersionHistory {
  fileId: number;
  fileName: string;
  versionNumber: number;
  storageKey: string;
  fileSize: number;
  current: boolean;
  uploadedBy: number;
  uploadedAt: string;
}

export interface PoSetting {
  key: string;
  value: string;
  updatedAt: string;
}
