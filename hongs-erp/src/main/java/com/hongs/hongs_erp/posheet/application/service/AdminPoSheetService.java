package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.posheet.application.dto.response.SettingResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.TrashItemResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.VersionHistoryResponse;
import com.hongs.hongs_erp.posheet.application.port.in.AdminPoSheetUseCase;
import com.hongs.hongs_erp.posheet.application.port.out.*;
import com.hongs.hongs_erp.posheet.domain.PoFile;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import com.hongs.hongs_erp.posheet.domain.PoSetting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminPoSheetService implements AdminPoSheetUseCase {

    private final PoFolderRepository folderRepository;
    private final PoFileRepository fileRepository;
    private final FileVersionRepository versionRepository;
    private final PoSettingRepository settingRepository;
    private final StoragePort storage;

    public AdminPoSheetService(PoFolderRepository folderRepository, PoFileRepository fileRepository,
                               FileVersionRepository versionRepository,
                               PoSettingRepository settingRepository, StoragePort storage) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
        this.settingRepository = settingRepository;
        this.storage = storage;
    }

    @Override
    public List<TrashItemResponse> listTrash() {
        LocalDateTime far = LocalDateTime.now().plusYears(100);
        List<TrashItemResponse> result = new ArrayList<>();
        folderRepository.findPurgeable(far).stream()
                .map(f -> new TrashItemResponse("FOLDER", f.getId(), f.getName(),
                        f.getDeletedAt(), f.getPurgeAt(), f.getDeleteBatchId()))
                .forEach(result::add);
        fileRepository.findPurgeable(far).stream()
                .map(f -> new TrashItemResponse("FILE", f.getId(), f.getName(),
                        f.getDeletedAt(), f.getPurgeAt(), f.getDeleteBatchId()))
                .forEach(result::add);
        return result;
    }

    @Override
    @Transactional
    public int purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<PoFolder> folders = folderRepository.findPurgeable(now);
        List<PoFile> files = fileRepository.findPurgeable(now);

        List<String> keys = files.stream()
                .flatMap(f -> versionRepository.findAllByFileId(f.getId()).stream()
                        .map(v -> v.getStorageKey()))
                .toList();
        if (!keys.isEmpty()) storage.deleteBatch(keys);

        if (!folders.isEmpty())
            folderRepository.markPurged(folders.stream().map(PoFolder::getId).toList());
        if (!files.isEmpty())
            fileRepository.markPurged(files.stream().map(PoFile::getId).toList());

        return folders.size() + files.size();
    }

    @Override
    public List<VersionHistoryResponse> listVersionHistory() {
        return fileRepository.findPurgeable(LocalDateTime.now().plusYears(100)).stream()
                .flatMap(f -> versionRepository.findAllByFileId(f.getId()).stream()
                        .map(v -> new VersionHistoryResponse(f.getId(), f.getName(),
                                v.getVersionNumber(), v.getStorageKey(), v.getFileSize(),
                                v.isCurrent(), v.getUploadedBy(), v.getUploadedAt())))
                .toList();
    }

    @Override
    public List<SettingResponse> listSettings() {
        return settingRepository.findAll().stream()
                .map(s -> new SettingResponse(s.getKey(), s.getValue(), s.getUpdatedAt()))
                .toList();
    }

    @Override
    @Transactional
    public void updateSetting(String key, String value, Long adminId) {
        settingRepository.save(PoSetting.of(key, value, adminId));
    }
}
