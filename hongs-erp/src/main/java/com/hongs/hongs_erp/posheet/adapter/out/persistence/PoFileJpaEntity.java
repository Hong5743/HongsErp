package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.domain.PoFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_files", indexes = {
    @Index(name = "idx_psfi_folder", columnList = "folder_id"),
    @Index(name = "idx_psfi_trash",  columnList = "deleted_at,purge_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PoFileJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "folder_id", nullable = false) private Long folderId;
    @Column(nullable = false) private String name;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private Long deletedBy;
    @Column(name = "delete_batch_id", length = 36) private String deleteBatchId;
    @Column(name = "purge_at") private LocalDateTime purgeAt;
    @Column(name = "purged_at") private LocalDateTime purgedAt;

    PoFileJpaEntity(Long folderId, String name, Long createdBy, LocalDateTime createdAt) {
        this.folderId = folderId; this.name = name; this.createdBy = createdBy; this.createdAt = createdAt;
    }

    static PoFileJpaEntity fromDomain(PoFile f) {
        var e = new PoFileJpaEntity(f.getFolderId(), f.getName(), f.getCreatedBy(), f.getCreatedAt());
        e.id = f.getId(); e.deletedAt = f.getDeletedAt(); e.deletedBy = f.getDeletedBy();
        e.deleteBatchId = f.getDeleteBatchId(); e.purgeAt = f.getPurgeAt(); e.purgedAt = f.getPurgedAt();
        return e;
    }

    PoFile toDomain() {
        return new PoFile(id, folderId, name, createdBy, createdAt,
                deletedAt, deletedBy, deleteBatchId, purgeAt, purgedAt);
    }
}
