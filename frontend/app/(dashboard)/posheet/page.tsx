'use client';

import { useState } from 'react';
import FolderTree from '@/components/posheet/FolderTree';
import FileList from '@/components/posheet/FileList';
import DragDropUpload from '@/components/posheet/DragDropUpload';
import PdfPreview from '@/components/posheet/PdfPreview';

export default function PoSheetPage() {
  const [selectedFolderId, setSelectedFolderId] = useState<number | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  // TODO: 실제 role/canWrite는 AuthContext 또는 /api/me 에서 조회
  const canWrite = true;

  const refresh = () => setRefreshKey(k => k + 1);

  return (
    <div className="flex h-full">
      {/* 왼쪽: 폴더 트리 */}
      <aside className="w-64 border-r overflow-y-auto p-2">
        <h2 className="font-semibold text-sm text-gray-700 mb-2 px-2">posheet</h2>
        <FolderTree
          selectedId={selectedFolderId}
          onSelect={setSelectedFolderId}
          canWrite={canWrite}
          onRefresh={refresh}
        />
      </aside>

      {/* 가운데: 파일 목록 */}
      <main className="flex-1 overflow-y-auto p-4">
        {selectedFolderId == null ? (
          <div className="text-gray-400 text-sm mt-8 text-center">
            왼쪽에서 폴더를 선택하세요
          </div>
        ) : (
          <>
            {canWrite && (
              <DragDropUpload
                folderId={selectedFolderId}
                onUploadComplete={refresh}
              />
            )}
            <FileList
              key={refreshKey}
              folderId={selectedFolderId}
              canWrite={canWrite}
              onPreview={setPreviewUrl}
              onChange={refresh}
            />
          </>
        )}
      </main>

      {/* 오른쪽: PDF 미리보기 */}
      {previewUrl && (
        <aside className="w-96 border-l">
          <PdfPreview url={previewUrl} onClose={() => setPreviewUrl(null)} />
        </aside>
      )}
    </div>
  );
}
