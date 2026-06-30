export interface FileWithPath {
  file: File;
  relativePath: string; // 예: "계약서/2024/1월/doc.pdf"
}

// DataTransferItem 배열에서 재귀적으로 모든 파일을 수집
export async function collectFiles(items: DataTransferItemList): Promise<FileWithPath[]> {
  const results: FileWithPath[] = [];
  const entries: FileSystemEntry[] = [];

  for (let i = 0; i < items.length; i++) {
    const entry = items[i].webkitGetAsEntry();
    if (entry) entries.push(entry);
  }

  await Promise.all(entries.map(entry => traverse(entry, '', results)));
  return results;
}

async function traverse(entry: FileSystemEntry, basePath: string, results: FileWithPath[]): Promise<void> {
  const currentPath = basePath ? `${basePath}/${entry.name}` : entry.name;

  if (entry.isFile) {
    const file = await getFile(entry as FileSystemFileEntry);
    results.push({ file, relativePath: currentPath });
  } else if (entry.isDirectory) {
    const children = await readDirectory(entry as FileSystemDirectoryEntry);
    await Promise.all(children.map(child => traverse(child, currentPath, results)));
  }
}

function getFile(entry: FileSystemFileEntry): Promise<File> {
  return new Promise((resolve, reject) => entry.file(resolve, reject));
}

async function readDirectory(entry: FileSystemDirectoryEntry): Promise<FileSystemEntry[]> {
  const reader = entry.createReader();
  const all: FileSystemEntry[] = [];

  // readEntries는 최대 100개씩 반환 — 루프로 전체 수집
  const read = (): Promise<void> => new Promise((resolve, reject) =>
    reader.readEntries(entries => {
      if (entries.length === 0) { resolve(); return; }
      all.push(...entries);
      read().then(resolve, reject);
    }, reject)
  );
  await read();
  return all;
}

// 고유 폴더 경로 목록을 깊이(depth) 기준 정렬 — 부모 먼저 생성하기 위함
// "a/b/c" → depth 3, "a/b" → depth 2, "a" → depth 1
// 정렬 후: ["a", "a/b", "a/b/c"] — O(k log k)
export function sortFolderPathsByDepth(paths: string[]): string[] {
  return [...new Set(paths)].sort((a, b) => {
    const da = a.split('/').length;
    const db = b.split('/').length;
    return da !== db ? da - db : a.localeCompare(b);
  });
}

// 상대 경로에서 폴더 경로들을 추출
// "계약서/2024/doc.pdf" → ["계약서", "계약서/2024"]
export function extractFolderPaths(files: FileWithPath[]): string[] {
  const paths = new Set<string>();
  for (const { relativePath } of files) {
    const parts = relativePath.split('/');
    for (let i = 1; i < parts.length; i++) {
      paths.add(parts.slice(0, i).join('/'));
    }
  }
  return [...paths];
}
