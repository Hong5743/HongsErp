package com.hongs.hongs_erp.posheet.application.dto.response;

import java.time.LocalDateTime;

public record PermissionResponse(Long id, Long employeeId, String employeeName,
                                 Long grantedBy, LocalDateTime grantedAt,
                                 boolean active, LocalDateTime revokedAt) {}
