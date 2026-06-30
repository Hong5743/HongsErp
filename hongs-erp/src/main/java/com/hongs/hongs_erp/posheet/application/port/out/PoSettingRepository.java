package com.hongs.hongs_erp.posheet.application.port.out;

import com.hongs.hongs_erp.posheet.domain.PoSetting;
import java.util.List;
import java.util.Optional;

public interface PoSettingRepository {
    PoSetting save(PoSetting setting);
    Optional<PoSetting> findByKey(String key);
    List<PoSetting> findAll();
}
