package com.hongs.hongs_erp.posheet.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class FileVersion {
    private Long id;
    private Long fileId;
    private int versionNumber;
    private String storageKey;
    private long fileSize;
    private boolean current;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;

    public FileVersion(Long id, Long fileId, int versionNumber, String storageKey,
                       long fileSize, boolean current, Long uploadedBy, LocalDateTime uploadedAt) {
        this.id = id; this.fileId = fileId; this.versionNumber = versionNumber;
        this.storageKey = storageKey; this.fileSize = fileSize; this.current = current;
        this.uploadedBy = uploadedBy; this.uploadedAt = uploadedAt;
    }

    public static FileVersion create(Long fileId, int versionNumber, String storageKey,
                                     long fileSize, Long uploadedBy) {
        return new FileVersion(null, fileId, versionNumber, storageKey,
                fileSize, true, uploadedBy, LocalDateTime.now());
    }
}
