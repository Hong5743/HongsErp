package com.hongs.hongs_erp.posheet.application.port.in;

import com.hongs.hongs_erp.posheet.application.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionUseCase {
    void grant(Long employeeId, Long adminId);
    void revoke(Long employeeId, Long adminId);
    List<PermissionResponse> listAll();
}
