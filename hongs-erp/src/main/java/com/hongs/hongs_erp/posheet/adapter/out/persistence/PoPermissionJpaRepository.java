package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface PoPermissionJpaRepository extends JpaRepository<PoPermissionJpaEntity, Long> {
    Optional<PoPermissionJpaEntity> findByEmployeeIdAndRevokedAtIsNull(Long employeeId);
    boolean existsByEmployeeIdAndRevokedAtIsNull(Long employeeId);
}
