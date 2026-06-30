package com.hongs.hongs_erp.posheet.application.dto.response;

import com.hongs.hongs_erp.posheet.domain.PoFolder;

public record FolderResponse(Long id, String name, Long parentId, String path) {
    public static FolderResponse from(PoFolder f) {
        return new FolderResponse(f.getId(), f.getName(), f.getParentId(), f.getPath());
    }
}
