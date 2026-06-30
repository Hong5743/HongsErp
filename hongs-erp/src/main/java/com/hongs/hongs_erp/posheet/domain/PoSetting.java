package com.hongs.hongs_erp.posheet.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PoSetting {
    public static final String TRASH_RETENTION_DAYS = "trash.retention_days";

    private String key;
    private String value;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public PoSetting(String key, String value, Long updatedBy, LocalDateTime updatedAt) {
        this.key = key; this.value = value; this.updatedBy = updatedBy; this.updatedAt = updatedAt;
    }

    public static PoSetting of(String key, String value, Long updatedBy) {
        return new PoSetting(key, value, updatedBy, LocalDateTime.now());
    }

    public int asInt() { return Integer.parseInt(value); }
}
