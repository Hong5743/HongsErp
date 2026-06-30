package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

interface FileVersionJpaRepository extends JpaRepository<FileVersionJpaEntity, Long> {
    Optional<FileVersionJpaEntity> findByFileIdAndCurrentTrue(Long fileId);
    List<FileVersionJpaEntity> findByFileIdOrderByVersionNumberDesc(Long fileId);
    int countByFileId(Long fileId);

    @Modifying
    @Query("UPDATE FileVersionJpaEntity v SET v.current = false WHERE v.fileId = :fileId")
    void deactivateAllByFileId(@Param("fileId") Long fileId);
}
