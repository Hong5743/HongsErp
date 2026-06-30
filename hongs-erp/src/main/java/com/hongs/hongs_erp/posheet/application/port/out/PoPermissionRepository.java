package com.hongs.hongs_erp.posheet.application.port.out;

import com.hongs.hongs_erp.posheet.domain.PoPermission;
import java.util.List;
import java.util.Optional;

public interface PoPermissionRepository {
    PoPermission save(PoPermission permission);
    boolean hasActivePermission(Long employeeId);
    void revoke(Long permissionId, Long revokedBy);
    List<PoPermission> findAll();
    Optional<PoPermission> findActiveByEmployeeId(Long employeeId);
}
