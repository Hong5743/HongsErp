package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

interface PoFolderJpaRepository extends JpaRepository<PoFolderJpaEntity, Long> {

    List<PoFolderJpaEntity> findByParentIdAndDeletedAtIsNull(Long parentId);

    List<PoFolderJpaEntity> findByPathStartingWithAndDeletedAtIsNull(String pathPrefix);

    @Query("SELECT f FROM PoFolderJpaEntity f " +
           "WHERE f.purgeAt <= :before AND f.purgedAt IS NULL AND f.deletedAt IS NOT NULL")
    List<PoFolderJpaEntity> findPurgeable(@Param("before") LocalDateTime before);

    @Modifying
    @Query("UPDATE PoFolderJpaEntity f SET f.deletedAt = :now, f.deletedBy = :by, " +
           "f.deleteBatchId = :batchId, f.purgeAt = :purgeAt " +
           "WHERE f.path LIKE :prefix% AND f.deletedAt IS NULL")
    void softDeleteTree(@Param("prefix") String prefix, @Param("by") Long by,
                        @Param("batchId") String batchId, @Param("now") LocalDateTime now,
                        @Param("purgeAt") LocalDateTime purgeAt);

    @Modifying
    @Query("UPDATE PoFolderJpaEntity f SET f.purgedAt = :now WHERE f.id IN :ids")
    void markPurged(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);
}
