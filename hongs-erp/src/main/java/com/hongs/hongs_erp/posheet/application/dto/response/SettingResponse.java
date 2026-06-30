package com.hongs.hongs_erp.posheet.application.dto.response;

import java.time.LocalDateTime;

public record SettingResponse(String key, String value, LocalDateTime updatedAt) {}
