package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.port.out.PoPermissionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class PoSheetPermissionChecker {
    private final PoPermissionRepository permissionRepository;
    public PoSheetPermissionChecker(PoPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void assertCanWrite(Long userId, User.Role role) {
        if (role == User.Role.ADMIN) return;
        if (!permissionRepository.hasActivePermission(userId))
            throw new AccessDeniedException("posheet 쓰기 권한이 없습니다");
    }
}
