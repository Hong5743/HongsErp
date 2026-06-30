package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.FileResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.PresignedUrlResponse;
import com.hongs.hongs_erp.posheet.application.port.in.FileUseCase;
import com.hongs.hongs_erp.posheet.application.port.out.*;
import com.hongs.hongs_erp.posheet.domain.FileVersion;
import com.hongs.hongs_erp.posheet.domain.PoFile;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FileService implements FileUseCase {

    private final PoFileRepository fileRepository;
    private final FileVersionRepository versionRepository;
    private final PoFolderRepository folderRepository;
    private final PoSettingRepository settingRepository;
    private final StoragePort storage;
    private final PoSheetPermissionChecker permissionChecker;

    public FileService(PoFileRepository fileRepository, FileVersionRepository versionRepository,
                       PoFolderRepository folderRepository, PoSettingRepository settingRepository,
                       StoragePort storage, PoSheetPermissionChecker permissionChecker) {
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
        this.folderRepository = folderRepository;
        this.settingRepository = settingRepository;
        this.storage = storage;
        this.permissionChecker = permissionChecker;
    }

    @Override
    @Transactional
    public FileResponse uploadFile(Long folderId, String fileName, InputStream data,
                                   long size, Long requesterId, User.Role role) {
        permissionChecker.assertCanWrite(requesterId, role);
        folderRepository.findById(folderId)
                .filter(PoFolder::isAlive)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다"));

        PoFile file = fileRepository.save(PoFile.create(folderId, fileName, requesterId));
        String key = "posheet/files/" + file.getId() + "/v1.pdf";
        storage.upload(key, data, size, "application/pdf");
        FileVersion version = versionRepository.save(
                FileVersion.create(file.getId(), 1, key, size, requesterId));
        return FileResponse.from(file, version);
    }

    @Override
    @Transactional
    public FileResponse replaceFile(Long fileId, String fileName, InputStream data,
                                    long size, Long requesterId, User.Role role) {
        permissionChecker.assertCanWrite(requesterId, role);
        PoFile file = fileRepository.findById(fileId)
                .filter(PoFile::isAlive)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        versionRepository.deactivateAllByFileId(fileId);
        int nextVersion = versionRepository.countByFileId(fileId) + 1;
        String key = "posheet/files/" + fileId + "/v" + nextVersion + ".pdf";
        storage.upload(key, data, size, "application/pdf");
        FileVersion version = versionRepository.save(
                FileVersion.create(fileId, nextVersion, key, size, requesterId));
        return FileResponse.from(file, version);
    }

    @Override
    public PresignedUrlResponse getPreviewUrl(Long fileId) {
        fileRepository.findById(fileId)
                .filter(PoFile::isAlive)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
        FileVersion current = versionRepository.findCurrentByFileId(fileId)
                .orElseThrow(() -> new IllegalStateException("현재 버전이 없습니다"));
        String url = storage.generatePresignedUrl(current.getStorageKey(), Duration.ofHours(1));
        return new PresignedUrlResponse(url, "1hour");
    }

    @Override
    public List<FileResponse> listFiles(Long folderId) {
        return fileRepository.findActiveByFolderId(folderId).stream()
                .map(f -> versionRepository.findCurrentByFileId(f.getId())
                        .map(v -> FileResponse.from(f, v)).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long requesterId, User.Role role) {
        permissionChecker.assertCanWrite(requesterId, role);
        PoFile file = fileRepository.findById(fileId)
                .filter(PoFile::isAlive)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
        int days = settingRepository.findByKey("trash.retention_days").map(s -> s.asInt()).orElse(30);
        String batchId = UUID.randomUUID().toString();
        fileRepository.save(file.softDelete(requesterId, batchId, LocalDateTime.now().plusDays(days)));
    }
}
