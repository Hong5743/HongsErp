'use client';

import { useState, useEffect } from 'react';
import { listFiles, getPreviewUrl, replaceFile, deleteFile } from '@/lib/api/posheet';
import type { PoFile } from '@/types/posheet';

interface Props {
  folderId: number;
  canWrite: boolean;
  onPreview: (url: string) => void;
  onChange: () => void;
}

export default function FileList({ folderId, canWrite, onPreview, onChange }: Props) {
  const [files, setFiles] = useState<PoFile[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      setLoading(true);
      setFiles(await listFiles(folderId));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [folderId]);

  const handlePreview = async (file: PoFile) => {
    const { url } = await getPreviewUrl(file.id);
    onPreview(url);
  };

  const handleReplace = async (file: PoFile) => {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = 'application/pdf';
    input.onchange = async () => {
      const f = input.files?.[0];
      if (!f) return;
      await replaceFile(file.id, f);
      load(); onChange();
    };
    input.click();
  };

  const handleDelete = async (file: PoFile) => {
    if (!window.confirm(`"${file.name}" 파일을 휴지통으로 이동할까요?`)) return;
    await deleteFile(file.id);
    load(); onChange();
  };

  const formatBytes = (b: number) => {
    if (b < 1024) return b + ' B';
    if (b < 1024 ** 2) return (b / 1024).toFixed(1) + ' KB';
    return (b / 1024 ** 2).toFixed(1) + ' MB';
  };

  if (loading) return <div className="text-sm text-gray-400">로딩중...</div>;
  if (files.length === 0) return <div className="text-sm text-gray-400 mt-4">파일이 없습니다.</div>;

  return (
    <table className="w-full text-sm mt-2">
      <thead>
        <tr className="text-left text-gray-500 border-b">
          <th className="pb-2">파일명</th>
          <th className="pb-2">크기</th>
          <th className="pb-2">버전</th>
          <th className="pb-2">작업</th>
        </tr>
      </thead>
      <tbody>
        {files.map(file => (
          <tr key={file.id} className="border-b hover:bg-gray-50">
            <td className="py-2">
              <button className="text-blue-600 hover:underline" onClick={() => handlePreview(file)}>
                📄 {file.name}
              </button>
            </td>
            <td className="py-2 text-gray-500">{formatBytes(file.fileSize)}</td>
            <td className="py-2 text-gray-500">v{file.currentVersion}</td>
            <td className="py-2 flex gap-2">
              {canWrite && (
                <>
                  <button
                    onClick={() => handleReplace(file)}
                    className="text-xs text-blue-500 hover:underline"
                  >교체</button>
                  <button
                    onClick={() => handleDelete(file)}
                    className="text-xs text-red-500 hover:underline"
                  >삭제</button>
                </>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
