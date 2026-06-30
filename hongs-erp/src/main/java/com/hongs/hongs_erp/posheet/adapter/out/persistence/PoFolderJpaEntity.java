package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.domain.PoFolder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_folders", indexes = {
    @Index(name = "idx_psf_parent", columnList = "parent_id"),
    @Index(name = "idx_psf_trash",  columnList = "deleted_at,purge_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PoFolderJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(name = "parent_id") private Long parentId;
    @Column(nullable = false, length = 2048) private String path;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;
    @Column(name = "delete_batch_id", length = 36) private String deleteBatchId;
    @Column(name = "purge_at") private LocalDateTime purgeAt;
    @Column(name = "purged_at") private LocalDateTime purgedAt;

    PoFolderJpaEntity(String name, Long parentId, String path, Long createdBy, LocalDateTime createdAt) {
        this.name = name; this.parentId = parentId; this.path = path;
        this.createdBy = createdBy; this.createdAt = createdAt;
    }

    static PoFolderJpaEntity fromDomain(PoFolder f) {
        var e = new PoFolderJpaEntity(f.getName(), f.getParentId(), f.getPath(),
                f.getCreatedBy(), f.getCreatedAt());
        e.id = f.getId();
        e.deletedAt = f.getDeletedAt(); e.deletedBy = f.getDeletedBy();
        e.deleteBatchId = f.getDeleteBatchId(); e.purgeAt = f.getPurgeAt(); e.purgedAt = f.getPurgedAt();
        return e;
    }

    PoFolder toDomain() {
        return new PoFolder(id, name, parentId, path, createdBy, createdAt,
                deletedAt, deletedBy, deleteBatchId, purgeAt, purgedAt);
    }

    void markPurged() { this.purgedAt = LocalDateTime.now(); }
}
