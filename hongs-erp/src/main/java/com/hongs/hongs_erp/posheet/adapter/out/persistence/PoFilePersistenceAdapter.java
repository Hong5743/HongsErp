package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.application.port.out.FileVersionRepository;
import com.hongs.hongs_erp.posheet.application.port.out.PoFileRepository;
import com.hongs.hongs_erp.posheet.domain.FileVersion;
import com.hongs.hongs_erp.posheet.domain.PoFile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PoFilePersistenceAdapter implements PoFileRepository, FileVersionRepository {
    private final PoFileJpaRepository fileJpa;
    private final FileVersionJpaRepository versionJpa;

    public PoFilePersistenceAdapter(PoFileJpaRepository fileJpa, FileVersionJpaRepository versionJpa) {
        this.fileJpa = fileJpa; this.versionJpa = versionJpa;
    }

    // PoFileRepository
    @Override public PoFile save(PoFile f) { return fileJpa.save(PoFileJpaEntity.fromDomain(f)).toDomain(); }
    @Override public Optional<PoFile> findById(Long id) { return fileJpa.findById(id).map(PoFileJpaEntity::toDomain); }
    @Override public List<PoFile> findActiveByFolderId(Long folderId) {
        return fileJpa.findByFolderIdAndDeletedAtIsNull(folderId).stream().map(PoFileJpaEntity::toDomain).toList();
    }
    @Override @Transactional
    public void softDeleteByFolderId(Long folderId, Long by, String batchId, LocalDateTime purgeAt) {
        fileJpa.softDeleteByFolderId(folderId, by, batchId, LocalDateTime.now(), purgeAt);
    }
    @Override public List<PoFile> findPurgeable(LocalDateTime before) {
        return fileJpa.findPurgeable(before).stream().map(PoFileJpaEntity::toDomain).toList();
    }
    @Override @Transactional
    public void markPurged(List<Long> ids) { fileJpa.markPurged(ids, LocalDateTime.now()); }

    // FileVersionRepository
    @Override public FileVersion save(FileVersion v) { return versionJpa.save(FileVersionJpaEntity.fromDomain(v)).toDomain(); }
    @Override public Optional<FileVersion> findCurrentByFileId(Long fileId) {
        return versionJpa.findByFileIdAndCurrentTrue(fileId).map(FileVersionJpaEntity::toDomain);
    }
    @Override public List<FileVersion> findAllByFileId(Long fileId) {
        return versionJpa.findByFileIdOrderByVersionNumberDesc(fileId).stream().map(FileVersionJpaEntity::toDomain).toList();
    }
    @Override @Transactional
    public void deactivateAllByFileId(Long fileId) { versionJpa.deactivateAllByFileId(fileId); }
    @Override public int countByFileId(Long fileId) { return versionJpa.countByFileId(fileId); }
}
