package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.domain.PoSetting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PoSettingJpaEntity {
    @Id @Column(name = "`key`") private String key;
    @Column(name = "`value`", nullable = false) private String value;
    @Column(name = "updated_by", nullable = false) private Long updatedBy;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    PoSettingJpaEntity(String key, String value, Long updatedBy, LocalDateTime updatedAt) {
        this.key = key; this.value = value; this.updatedBy = updatedBy; this.updatedAt = updatedAt;
    }

    static PoSettingJpaEntity fromDomain(PoSetting s) {
        return new PoSettingJpaEntity(s.getKey(), s.getValue(), s.getUpdatedBy(), s.getUpdatedAt());
    }

    PoSetting toDomain() { return new PoSetting(key, value, updatedBy, updatedAt); }
}
