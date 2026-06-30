package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

interface PoFileJpaRepository extends JpaRepository<PoFileJpaEntity, Long> {
    List<PoFileJpaEntity> findByFolderIdAndDeletedAtIsNull(Long folderId);

    @Query("SELECT f FROM PoFileJpaEntity f WHERE f.purgeAt <= :b AND f.purgedAt IS NULL AND f.deletedAt IS NOT NULL")
    List<PoFileJpaEntity> findPurgeable(@Param("b") LocalDateTime before);

    @Modifying
    @Query("UPDATE PoFileJpaEntity f SET f.deletedAt = :now, f.deletedBy = :by, " +
           "f.deleteBatchId = :batchId, f.purgeAt = :purgeAt " +
           "WHERE f.folderId = :folderId AND f.deletedAt IS NULL")
    void softDeleteByFolderId(@Param("folderId") Long folderId, @Param("by") Long by,
                              @Param("batchId") String batchId, @Param("now") LocalDateTime now,
                              @Param("purgeAt") LocalDateTime purgeAt);

    @Modifying
    @Query("UPDATE PoFileJpaEntity f SET f.purgedAt = :now WHERE f.id IN :ids")
    void markPurged(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);
}
