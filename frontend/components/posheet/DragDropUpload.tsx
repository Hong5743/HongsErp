'use client';

import { useState, useRef } from 'react';
import { collectFiles, extractFolderPaths, sortFolderPathsByDepth } from '@/lib/posheet/folderUpload';
import { createFolder, uploadFile } from '@/lib/api/posheet';
import type { PoFolder } from '@/types/posheet';

interface Props {
  folderId: number;
  onUploadComplete: () => void;
}

type UploadStatus = 'idle' | 'analyzing' | 'uploading' | 'done' | 'error';

export default function DragDropUpload({ folderId, onUploadComplete }: Props) {
  const [status, setStatus] = useState<UploadStatus>('idle');
  const [progress, setProgress] = useState({ done: 0, total: 0 });
  const [dragOver, setDragOver] = useState(false);
  const dropRef = useRef<HTMLDivElement>(null);

  const handleDrop = async (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    setStatus('analyzing');

    try {
      // 1. 드롭된 모든 파일 수집 (폴더 재귀 포함) — O(n)
      const files = await collectFiles(e.dataTransfer.items);
      if (files.length === 0) { setStatus('idle'); return; }

      const pdfFiles = files.filter(f => f.file.name.endsWith('.pdf'));
      setProgress({ done: 0, total: pdfFiles.length });
      setStatus('uploading');

      // 2. 폴더 경로 추출 + depth 기준 정렬 — O(k log k)
      const folderPaths = sortFolderPathsByDepth(extractFolderPaths(pdfFiles));

      // 3. path → folderId 맵핑 (부모 먼저 생성)
      const pathToId: Record<string, number> = { '': folderId };

      for (const folderPath of folderPaths) {
        const parts = folderPath.split('/');
        const folderName = parts[parts.length - 1];
        const parentPath = parts.slice(0, -1).join('/');
        const parentId = pathToId[parentPath] ?? folderId;

        const created: PoFolder = await createFolder(folderName, parentId);
        pathToId[folderPath] = created.id;
      }

      // 4. 파일 업로드 — O(n)
      for (const { file, relativePath } of pdfFiles) {
        const parts = relativePath.split('/');
        const parentPath = parts.slice(0, -1).join('/');
        const targetFolderId = pathToId[parentPath] ?? folderId;

        await uploadFile(targetFolderId, file);
        setProgress(p => ({ ...p, done: p.done + 1 }));
      }

      setStatus('done');
      setTimeout(() => { setStatus('idle'); onUploadComplete(); }, 1500);

    } catch (err) {
      console.error('업로드 오류:', err);
      setStatus('error');
      setTimeout(() => setStatus('idle'), 3000);
    }
  };

  const statusMessage = () => {
    switch (status) {
      case 'analyzing': return '폴더 구조 분석 중...';
      case 'uploading': return `업로드 중 ${progress.done}/${progress.total}`;
      case 'done': return `✅ 업로드 완료 (${progress.total}개)`;
      case 'error': return '❌ 업로드 중 오류가 발생했습니다';
      default: return '파일 또는 폴더를 여기에 드래그하세요';
    }
  };

  return (
    <div
      ref={dropRef}
      onDragOver={e => { e.preventDefault(); setDragOver(true); }}
      onDragLeave={() => setDragOver(false)}
      onDrop={handleDrop}
      className={`border-2 border-dashed rounded-lg p-4 mb-4 text-center text-sm transition-colors
        ${dragOver ? 'border-blue-400 bg-blue-50' : 'border-gray-300 text-gray-400'}
        ${status === 'uploading' ? 'border-blue-300 bg-blue-50' : ''}
        ${status === 'done' ? 'border-green-400 bg-green-50 text-green-600' : ''}
        ${status === 'error' ? 'border-red-400 bg-red-50 text-red-600' : ''}`}
    >
      {status === 'uploading' && (
        <div className="mb-2">
          <div className="bg-gray-200 rounded-full h-1.5">
            <div
              className="bg-blue-500 h-1.5 rounded-full transition-all"
              style={{ width: `${progress.total ? (progress.done / progress.total) * 100 : 0}%` }}
            />
          </div>
        </div>
      )}
      <p>{statusMessage()}</p>
      {status === 'idle' && (
        <p className="text-xs mt-1 text-gray-300">
          윈도우 탐색기에서 폴더째로 드래그해도 하위 구조가 유지됩니다 (PDF만 업로드)
        </p>
      )}
    </div>
  );
}
