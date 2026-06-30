'use client';

import { useState, useEffect } from 'react';
import { listFolders, createFolder, deleteFolder } from '@/lib/api/posheet';
import type { PoFolder } from '@/types/posheet';

interface Props {
  parentId?: number;
  depth?: number;
  selectedId: number | null;
  onSelect: (id: number) => void;
  canWrite: boolean;
  onRefresh?: () => void;
}

export default function FolderTree({
  parentId, depth = 0, selectedId, onSelect, canWrite, onRefresh,
}: Props) {
  const [folders, setFolders] = useState<PoFolder[]>([]);
  const [expanded, setExpanded] = useState<Set<number>>(new Set());
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      setLoading(true);
      const data = await listFolders(parentId);
      setFolders(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [parentId]);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    await createFolder(newName.trim(), parentId);
    setNewName(''); setCreating(false);
    load(); onRefresh?.();
  };

  const handleDelete = async (folder: PoFolder, e: React.MouseEvent) => {
    e.stopPropagation();
    const confirmed = window.confirm(
      `"${folder.name}" 폴더와 하위 항목 전체가 휴지통으로 이동됩니다. 계속할까요?`
    );
    if (!confirmed) return;
    await deleteFolder(folder.id);
    load(); onRefresh?.();
  };

  const toggle = (id: number) =>
    setExpanded(prev => {
      const s = new Set(prev);
      s.has(id) ? s.delete(id) : s.add(id);
      return s;
    });

  if (loading) return <div className="pl-4 text-sm text-gray-400">로딩중...</div>;

  return (
    <ul className="select-none">
      {folders.map(folder => (
        <li key={folder.id}>
          <div
            className={`flex items-center gap-1 px-2 py-1 rounded cursor-pointer text-sm
              hover:bg-gray-100 ${selectedId === folder.id ? 'bg-blue-100 font-medium' : ''}`}
            style={{ paddingLeft: `${depth * 16 + 8}px` }}
            onClick={() => { onSelect(folder.id); toggle(folder.id); }}
          >
            <span className="text-gray-400">{expanded.has(folder.id) ? '▼' : '▶'}</span>
            <span>📁 {folder.name}</span>
            {canWrite && (
              <button
                className="ml-auto text-gray-400 hover:text-red-500 text-xs"
                onClick={(e) => handleDelete(folder, e)}
              >✕</button>
            )}
          </div>
          {expanded.has(folder.id) && (
            <FolderTree
              parentId={folder.id} depth={depth + 1}
              selectedId={selectedId} onSelect={onSelect}
              canWrite={canWrite} onRefresh={load}
            />
          )}
        </li>
      ))}
      {canWrite && (
        <li style={{ paddingLeft: `${depth * 16 + 8}px` }}>
          {creating ? (
            <div className="flex gap-1 px-2 py-1">
              <input
                autoFocus className="border rounded px-1 text-sm flex-1"
                value={newName} onChange={e => setNewName(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') handleCreate();
                  if (e.key === 'Escape') setCreating(false);
                }}
                placeholder="폴더 이름"
              />
              <button onClick={handleCreate} className="text-blue-500 text-sm">확인</button>
              <button onClick={() => setCreating(false)} className="text-gray-400 text-sm">취소</button>
            </div>
          ) : (
            <button
              className="px-2 py-1 text-sm text-gray-400 hover:text-blue-500"
              onClick={() => setCreating(true)}
            >
              + 폴더 추가
            </button>
          )}
        </li>
      )}
    </ul>
  );
}
