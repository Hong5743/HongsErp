package com.hongs.hongs_erp.posheet.application.port.out;

import com.hongs.hongs_erp.posheet.domain.PoFolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PoFolderRepository {
    PoFolder save(PoFolder folder);
    Optional<PoFolder> findById(Long id);
    List<PoFolder> findActiveByParentId(Long parentId);
    List<PoFolder> findActiveByPathStartingWith(String prefix);
    void softDeleteTree(String pathPrefix, Long deletedBy, String batchId, LocalDateTime purgeAt);
    List<PoFolder> findPurgeable(LocalDateTime before);
    void markPurged(List<Long> ids);
}
