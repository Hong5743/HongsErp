package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.domain.FileVersion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_file_versions",
    uniqueConstraints = @UniqueConstraint(name = "uk_psv_file_version", columnNames = {"file_id", "version_number"}),
    indexes = @Index(name = "idx_psv_current", columnList = "file_id,is_current"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FileVersionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "file_id", nullable = false) private Long fileId;
    @Column(name = "version_number", nullable = false) private int versionNumber;
    @Column(name = "storage_key", nullable = false, length = 1024) private String storageKey;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "is_current", nullable = false) private boolean current;
    @Column(name = "uploaded_by", nullable = false) private Long uploadedBy;
    @Column(name = "uploaded_at", nullable = false) private LocalDateTime uploadedAt;

    FileVersionJpaEntity(Long fileId, int versionNumber, String storageKey,
                         long fileSize, boolean current, Long uploadedBy, LocalDateTime uploadedAt) {
        this.fileId = fileId; this.versionNumber = versionNumber; this.storageKey = storageKey;
        this.fileSize = fileSize; this.current = current; this.uploadedBy = uploadedBy; this.uploadedAt = uploadedAt;
    }

    static FileVersionJpaEntity fromDomain(FileVersion v) {
        var e = new FileVersionJpaEntity(v.getFileId(), v.getVersionNumber(), v.getStorageKey(),
                v.getFileSize(), v.isCurrent(), v.getUploadedBy(), v.getUploadedAt());
        e.id = v.getId();
        return e;
    }

    FileVersion toDomain() {
        return new FileVersion(id, fileId, versionNumber, storageKey, fileSize, current, uploadedBy, uploadedAt);
    }

    void deactivate() { this.current = false; }
}
