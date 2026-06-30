package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.domain.PoPermission;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_permissions",
    indexes = @Index(name = "idx_psp_employee", columnList = "employee_id,revoked_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PoPermissionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "employee_id", nullable = false) private Long employeeId;
    @Column(name = "granted_by", nullable = false) private Long grantedBy;
    @Column(name = "granted_at", nullable = false) private LocalDateTime grantedAt;
    @Column(name = "revoked_at") private LocalDateTime revokedAt;
    @Column(name = "revoked_by") private Long revokedBy;

    PoPermissionJpaEntity(Long employeeId, Long grantedBy, LocalDateTime grantedAt) {
        this.employeeId = employeeId; this.grantedBy = grantedBy; this.grantedAt = grantedAt;
    }

    static PoPermissionJpaEntity fromDomain(PoPermission p) {
        var e = new PoPermissionJpaEntity(p.getEmployeeId(), p.getGrantedBy(), p.getGrantedAt());
        e.id = p.getId(); e.revokedAt = p.getRevokedAt(); e.revokedBy = p.getRevokedBy();
        return e;
    }

    PoPermission toDomain() {
        return new PoPermission(id, employeeId, grantedBy, grantedAt, revokedAt, revokedBy);
    }

    void revoke(Long by) { this.revokedAt = LocalDateTime.now(); this.revokedBy = by; }
}
