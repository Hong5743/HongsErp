'use client';

interface Props {
  url: string;
  onClose: () => void;
}

export default function PdfPreview({ url, onClose }: Props) {
  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between p-2 border-b">
        <span className="text-sm font-medium">PDF 미리보기</span>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-700">✕</button>
      </div>
      {/* 브라우저 기본 PDF 뷰어 — 별도 라이브러리 불필요 */}
      <iframe
        src={url}
        className="flex-1 w-full border-0"
        title="PDF 미리보기"
      />
    </div>
  );
}
