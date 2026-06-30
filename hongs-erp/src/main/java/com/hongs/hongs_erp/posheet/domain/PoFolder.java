package com.hongs.hongs_erp.posheet.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PoFolder {
    private Long id;
    private String name;
    private Long parentId;
    private String path;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private String deleteBatchId;
    private LocalDateTime purgeAt;
    private LocalDateTime purgedAt;

    public PoFolder(Long id, String name, Long parentId, String path, Long createdBy,
                    LocalDateTime createdAt, LocalDateTime deletedAt, Long deletedBy,
                    String deleteBatchId, LocalDateTime purgeAt, LocalDateTime purgedAt) {
        this.id = id; this.name = name; this.parentId = parentId; this.path = path;
        this.createdBy = createdBy; this.createdAt = createdAt; this.deletedAt = deletedAt;
        this.deletedBy = deletedBy; this.deleteBatchId = deleteBatchId;
        this.purgeAt = purgeAt; this.purgedAt = purgedAt;
    }

    public static PoFolder createRoot(String name, Long createdBy) {
        return new PoFolder(null, name, null, "/" + name + "/", createdBy,
                LocalDateTime.now(), null, null, null, null, null);
    }

    public static PoFolder createSub(String name, Long parentId, String parentPath, Long createdBy) {
        return new PoFolder(null, name, parentId, parentPath + name + "/", createdBy,
                LocalDateTime.now(), null, null, null, null, null);
    }

    public PoFolder softDelete(Long deletedBy, String batchId, LocalDateTime purgeAt) {
        return new PoFolder(id, name, parentId, path, createdBy, createdAt,
                LocalDateTime.now(), deletedBy, batchId, purgeAt, null);
    }

    public boolean isAlive() { return deletedAt == null; }
}
