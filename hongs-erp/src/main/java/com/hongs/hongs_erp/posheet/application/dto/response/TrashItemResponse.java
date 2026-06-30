package com.hongs.hongs_erp.posheet.application.dto.response;

import java.time.LocalDateTime;

public record TrashItemResponse(String type, Long id, String name,
                                LocalDateTime deletedAt, LocalDateTime purgeAt, String deleteBatchId) {}
