package com.hongs.hongs_erp.posheet.application.dto.response;

import com.hongs.hongs_erp.posheet.domain.FileVersion;
import com.hongs.hongs_erp.posheet.domain.PoFile;

import java.time.LocalDateTime;

public record FileResponse(Long id, Long folderId, String name,
                           int currentVersion, long fileSize, LocalDateTime createdAt) {
    public static FileResponse from(PoFile file, FileVersion version) {
        return new FileResponse(file.getId(), file.getFolderId(), file.getName(),
                version.getVersionNumber(), version.getFileSize(), file.getCreatedAt());
    }
}
