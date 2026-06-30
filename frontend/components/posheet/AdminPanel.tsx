'use client';

import { useState, useEffect } from 'react';
import {
  listTrash, purgeTrash,
  listVersionHistory,
  listSettings, updateSetting,
  listPermissions, grantPermission, revokePermission,
} from '@/lib/api/posheet';
import type { TrashItem, VersionHistory, PoSetting, PermissionResponse } from '@/types/posheet';

type Tab = 'trash' | 'versions' | 'permissions' | 'settings';

export default function AdminPanel() {
  const [tab, setTab] = useState<Tab>('trash');
  const [trash, setTrash] = useState<TrashItem[]>([]);
  const [versions, setVersions] = useState<VersionHistory[]>([]);
  const [settings, setSettings] = useState<PoSetting[]>([]);
  const [permissions, setPermissions] = useState<PermissionResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const load = async (t: Tab) => {
    setLoading(true);
    try {
      if (t === 'trash') setTrash(await listTrash());
      if (t === 'versions') setVersions(await listVersionHistory());
      if (t === 'settings') setSettings(await listSettings());
      if (t === 'permissions') setPermissions(await listPermissions());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(tab); }, [tab]);

  const handlePurge = async () => {
    if (!window.confirm('만료된 항목을 즉시 완전 삭제할까요?')) return;
    const { purged } = await purgeTrash();
    alert(`${purged}개 항목이 완전 삭제되었습니다.`);
    load('trash');
  };

  const handleUpdateSetting = async (key: string, currentValue: string) => {
    const newVal = window.prompt('새 값을 입력하세요:', currentValue);
    if (newVal == null || newVal === currentValue) return;
    await updateSetting(key, newVal);
    load('settings');
  };

  const handleGrant = async (empId: number) => {
    await grantPermission(empId);
    load('permissions');
  };

  const handleRevoke = async (empId: number) => {
    if (!window.confirm('권한을 회수할까요?')) return;
    await revokePermission(empId);
    load('permissions');
  };

  const tabs: { key: Tab; label: string }[] = [
    { key: 'trash', label: '휴지통' },
    { key: 'versions', label: '버전 이력' },
    { key: 'permissions', label: '권한 관리' },
    { key: 'settings', label: '설정' },
  ];

  return (
    <div className="p-4">
      <h1 className="text-lg font-semibold mb-4">posheet 관리</h1>

      <div className="flex gap-2 mb-4 border-b">
        {tabs.map(t => (
          <button key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-sm -mb-px border-b-2 transition-colors
              ${tab === t.key ? 'border-blue-500 text-blue-600 font-medium' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          >{t.label}</button>
        ))}
      </div>

      {loading && <div className="text-sm text-gray-400">로딩중...</div>}

      {/* 휴지통 탭 */}
      {!loading && tab === 'trash' && (
        <div>
          <div className="flex justify-between items-center mb-3">
            <span className="text-sm text-gray-500">{trash.length}개 항목</span>
            <button
              onClick={handlePurge}
              className="text-sm bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
            >만료된 항목 즉시 삭제</button>
          </div>
          {trash.length === 0 ? (
            <p className="text-sm text-gray-400">휴지통이 비어 있습니다.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-2">유형</th><th className="pb-2">이름</th>
                  <th className="pb-2">삭제일</th><th className="pb-2">완전삭제 예정</th>
                </tr>
              </thead>
              <tbody>
                {trash.map(item => (
                  <tr key={`${item.type}-${item.id}`} className="border-b hover:bg-gray-50">
                    <td className="py-2">{item.type === 'FOLDER' ? '📁' : '📄'}</td>
                    <td className="py-2">{item.name}</td>
                    <td className="py-2 text-gray-500">{new Date(item.deletedAt).toLocaleDateString('ko-KR')}</td>
                    <td className="py-2 text-red-400">{new Date(item.purgeAt).toLocaleDateString('ko-KR')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* 버전 이력 탭 */}
      {!loading && tab === 'versions' && (
        <div>
          {versions.length === 0 ? (
            <p className="text-sm text-gray-400">버전 이력이 없습니다.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-2">파일명</th><th className="pb-2">버전</th>
                  <th className="pb-2">크기</th><th className="pb-2">업로드일</th><th className="pb-2">현재</th>
                </tr>
              </thead>
              <tbody>
                {versions.map(v => (
                  <tr key={`${v.fileId}-${v.versionNumber}`} className="border-b hover:bg-gray-50">
                    <td className="py-2">{v.fileName}</td>
                    <td className="py-2">v{v.versionNumber}</td>
                    <td className="py-2 text-gray-500">{(v.fileSize / 1024).toFixed(1)} KB</td>
                    <td className="py-2 text-gray-500">{new Date(v.uploadedAt).toLocaleDateString('ko-KR')}</td>
                    <td className="py-2">{v.current ? '✅' : ''}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* 권한 관리 탭 */}
      {!loading && tab === 'permissions' && (
        <div>
          {permissions.length === 0 ? (
            <p className="text-sm text-gray-400">권한 기록이 없습니다.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-2">직원</th><th className="pb-2">부여일</th>
                  <th className="pb-2">상태</th><th className="pb-2">작업</th>
                </tr>
              </thead>
              <tbody>
                {permissions.map(p => (
                  <tr key={p.id} className="border-b hover:bg-gray-50">
                    <td className="py-2">{p.employeeName}</td>
                    <td className="py-2 text-gray-500">{new Date(p.grantedAt).toLocaleDateString('ko-KR')}</td>
                    <td className="py-2">
                      <span className={`px-2 py-0.5 rounded text-xs ${p.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {p.active ? '활성' : '회수됨'}
                      </span>
                    </td>
                    <td className="py-2">
                      {p.active ? (
                        <button onClick={() => handleRevoke(p.employeeId)}
                          className="text-xs text-red-500 hover:underline">회수</button>
                      ) : (
                        <button onClick={() => handleGrant(p.employeeId)}
                          className="text-xs text-blue-500 hover:underline">재부여</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* 설정 탭 */}
      {!loading && tab === 'settings' && (
        <div>
          {settings.length === 0 ? (
            <p className="text-sm text-gray-400">설정 항목이 없습니다.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-2">설정 키</th><th className="pb-2">값</th>
                  <th className="pb-2">최종 수정</th><th className="pb-2">작업</th>
                </tr>
              </thead>
              <tbody>
                {settings.map(s => (
                  <tr key={s.key} className="border-b hover:bg-gray-50">
                    <td className="py-2 font-mono text-xs">{s.key}</td>
                    <td className="py-2">{s.value}</td>
                    <td className="py-2 text-gray-500">{new Date(s.updatedAt).toLocaleDateString('ko-KR')}</td>
                    <td className="py-2">
                      <button onClick={() => handleUpdateSetting(s.key, s.value)}
                        className="text-xs text-blue-500 hover:underline">수정</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
