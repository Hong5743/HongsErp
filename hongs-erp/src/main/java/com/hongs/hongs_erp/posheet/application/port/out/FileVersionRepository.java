package com.hongs.hongs_erp.posheet.application.port.out;

import com.hongs.hongs_erp.posheet.domain.FileVersion;
import java.util.List;
import java.util.Optional;

public interface FileVersionRepository {
    FileVersion save(FileVersion version);
    Optional<FileVersion> findCurrentByFileId(Long fileId);
    List<FileVersion> findAllByFileId(Long fileId);
    void deactivateAllByFileId(Long fileId);
    int countByFileId(Long fileId);
}
