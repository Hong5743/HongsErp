package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.employee.application.port.out.UserRepository;
import com.hongs.hongs_erp.employee.domain.User;
import com.hongs.hongs_erp.posheet.application.dto.response.PermissionResponse;
import com.hongs.hongs_erp.posheet.application.port.in.PermissionUseCase;
import com.hongs.hongs_erp.posheet.application.port.out.PoPermissionRepository;
import com.hongs.hongs_erp.posheet.domain.PoPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionService implements PermissionUseCase {

    private final PoPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public PermissionService(PoPermissionRepository permissionRepository, UserRepository userRepository) {
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void grant(Long employeeId, Long adminId) {
        if (permissionRepository.hasActivePermission(employeeId)) return;
        permissionRepository.save(PoPermission.grant(employeeId, adminId));
    }

    @Override
    @Transactional
    public void revoke(Long employeeId, Long adminId) {
        permissionRepository.findActiveByEmployeeId(employeeId)
                .ifPresent(p -> permissionRepository.revoke(p.getId(), adminId));
    }

    @Override
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream().map(p -> {
            String name = userRepository.findById(p.getEmployeeId())
                    .map(User::getName).orElse("알 수 없음");
            return new PermissionResponse(p.getId(), p.getEmployeeId(), name,
                    p.getGrantedBy(), p.getGrantedAt(), p.isActive(), p.getRevokedAt());
        }).toList();
    }
}
