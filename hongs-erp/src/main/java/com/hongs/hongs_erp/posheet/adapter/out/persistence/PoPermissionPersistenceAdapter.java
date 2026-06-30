package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.application.port.out.PoPermissionRepository;
import com.hongs.hongs_erp.posheet.domain.PoPermission;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class PoPermissionPersistenceAdapter implements PoPermissionRepository {
    private final PoPermissionJpaRepository jpa;
    public PoPermissionPersistenceAdapter(PoPermissionJpaRepository jpa) { this.jpa = jpa; }

    @Override public PoPermission save(PoPermission p) { return jpa.save(PoPermissionJpaEntity.fromDomain(p)).toDomain(); }
    @Override public boolean hasActivePermission(Long id) { return jpa.existsByEmployeeIdAndRevokedAtIsNull(id); }
    @Override @Transactional
    public void revoke(Long id, Long by) { jpa.findById(id).ifPresent(e -> e.revoke(by)); }
    @Override public List<PoPermission> findAll() { return jpa.findAll().stream().map(PoPermissionJpaEntity::toDomain).toList(); }
    @Override public Optional<PoPermission> findActiveByEmployeeId(Long empId) {
        return jpa.findByEmployeeIdAndRevokedAtIsNull(empId).map(PoPermissionJpaEntity::toDomain);
    }
}
