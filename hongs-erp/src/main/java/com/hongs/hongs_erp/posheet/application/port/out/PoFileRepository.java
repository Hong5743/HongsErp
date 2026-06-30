package com.hongs.hongs_erp.posheet.application.port.out;

import com.hongs.hongs_erp.posheet.domain.PoFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PoFileRepository {
    PoFile save(PoFile file);
    Optional<PoFile> findById(Long id);
    List<PoFile> findActiveByFolderId(Long folderId);
    void softDeleteByFolderId(Long folderId, Long by, String batchId, LocalDateTime purgeAt);
    List<PoFile> findPurgeable(LocalDateTime before);
    void markPurged(List<Long> ids);
}
