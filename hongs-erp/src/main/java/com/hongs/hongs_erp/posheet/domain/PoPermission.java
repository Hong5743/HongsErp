package com.hongs.hongs_erp.posheet.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PoPermission {
    private Long id;
    private Long employeeId;
    private Long grantedBy;
    private LocalDateTime grantedAt;
    private LocalDateTime revokedAt;
    private Long revokedBy;

    public PoPermission(Long id, Long employeeId, Long grantedBy, LocalDateTime grantedAt,
                        LocalDateTime revokedAt, Long revokedBy) {
        this.id = id; this.employeeId = employeeId; this.grantedBy = grantedBy;
        this.grantedAt = grantedAt; this.revokedAt = revokedAt; this.revokedBy = revokedBy;
    }

    public static PoPermission grant(Long employeeId, Long grantedBy) {
        return new PoPermission(null, employeeId, grantedBy, LocalDateTime.now(), null, null);
    }

    public boolean isActive() { return revokedAt == null; }
}
