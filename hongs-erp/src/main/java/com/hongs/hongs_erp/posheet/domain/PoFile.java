package com.hongs.hongs_erp.posheet.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PoFile {
    private Long id;
    private Long folderId;
    private String name;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private String deleteBatchId;
    private LocalDateTime purgeAt;
    private LocalDateTime purgedAt;

    public PoFile(Long id, Long folderId, String name, Long createdBy, LocalDateTime createdAt,
                  LocalDateTime deletedAt, Long deletedBy, String deleteBatchId,
                  LocalDateTime purgeAt, LocalDateTime purgedAt) {
        this.id = id; this.folderId = folderId; this.name = name; this.createdBy = createdBy;
        this.createdAt = createdAt; this.deletedAt = deletedAt; this.deletedBy = deletedBy;
        this.deleteBatchId = deleteBatchId; this.purgeAt = purgeAt; this.purgedAt = purgedAt;
    }

    public static PoFile create(Long folderId, String name, Long createdBy) {
        return new PoFile(null, folderId, name, createdBy, LocalDateTime.now(),
                null, null, null, null, null);
    }

    public PoFile softDelete(Long deletedBy, String batchId, LocalDateTime purgeAt) {
        return new PoFile(id, folderId, name, createdBy, createdAt,
                LocalDateTime.now(), deletedBy, batchId, purgeAt, null);
    }

    public boolean isAlive() { return deletedAt == null; }
}
