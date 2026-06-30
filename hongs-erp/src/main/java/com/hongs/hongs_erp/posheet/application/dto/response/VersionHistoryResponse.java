package com.hongs.hongs_erp.posheet.application.dto.response;

import java.time.LocalDateTime;

public record VersionHistoryResponse(Long fileId, String fileName, int versionNumber,
                                     String storageKey, long fileSize, boolean current,
                                     Long uploadedBy, LocalDateTime uploadedAt) {}
