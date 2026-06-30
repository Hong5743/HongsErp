package com.hongs.hongs_erp.posheet.application.port.in;

import com.hongs.hongs_erp.posheet.application.dto.response.SettingResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.TrashItemResponse;
import com.hongs.hongs_erp.posheet.application.dto.response.VersionHistoryResponse;

import java.util.List;

public interface AdminPoSheetUseCase {
    List<TrashItemResponse> listTrash();
    int purgeExpired();
    List<VersionHistoryResponse> listVersionHistory();
    List<SettingResponse> listSettings();
    void updateSetting(String key, String value, Long adminId);
}
