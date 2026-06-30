package com.hongs.hongs_erp.posheet.adapter.out.persistence;

import com.hongs.hongs_erp.posheet.application.port.out.PoSettingRepository;
import com.hongs.hongs_erp.posheet.domain.PoSetting;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PoSettingPersistenceAdapter implements PoSettingRepository {
    private final PoSettingJpaRepository jpa;
    public PoSettingPersistenceAdapter(PoSettingJpaRepository jpa) { this.jpa = jpa; }

    @Override public PoSetting save(PoSetting s) { return jpa.save(PoSettingJpaEntity.fromDomain(s)).toDomain(); }
    @Override public Optional<PoSetting> findByKey(String key) { return jpa.findById(key).map(PoSettingJpaEntity::toDomain); }
    @Override public List<PoSetting> findAll() { return jpa.findAll().stream().map(PoSettingJpaEntity::toDomain).toList(); }
}
