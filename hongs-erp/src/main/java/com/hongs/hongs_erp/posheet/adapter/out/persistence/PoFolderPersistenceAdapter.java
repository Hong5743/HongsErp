package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.application.port.out.PoFolderRepository;
import com.hongs.hongs_erp.posheet.domain.PoFolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PoFolderPersistenceAdapter implements PoFolderRepository {
    private final PoFolderJpaRepository jpa;
    public PoFolderPersistenceAdapter(PoFolderJpaRepository jpa) { this.jpa = jpa; }

    @Override public PoFolder save(PoFolder f) { return jpa.save(PoFolderJpaEntity.fromDomain(f)).toDomain(); }
    @Override public Optional<PoFolder> findById(Long id) { return jpa.findById(id).map(PoFolderJpaEntity::toDomain); }

    @Override public List<PoFolder> findActiveByParentId(Long parentId) {
        return jpa.findByParentIdAndDeletedAtIsNull(parentId).stream().map(PoFolderJpaEntity::toDomain).toList();
    }

    @Override public List<PoFolder> findActiveByPathStartingWith(String prefix) {
        return jpa.findByPathStartingWithAndDeletedAtIsNull(prefix).stream()
                .filter(e -> !e.toDomain().getPath().equals(prefix))
                .map(PoFolderJpaEntity::toDomain).toList();
    }

    @Override @Transactional
    public void softDeleteTree(String prefix, Long by, String batchId, LocalDateTime purgeAt) {
        jpa.softDeleteTree(prefix, by, batchId, LocalDateTime.now(), purgeAt);
    }

    @Override public List<PoFolder> findPurgeable(LocalDateTime before) {
        return jpa.findPurgeable(before).stream().map(PoFolderJpaEntity::toDomain).toList();
    }

    @Override @Transactional
    public void markPurged(List<Long> ids) { jpa.markPurged(ids, LocalDateTime.now()); }
}
