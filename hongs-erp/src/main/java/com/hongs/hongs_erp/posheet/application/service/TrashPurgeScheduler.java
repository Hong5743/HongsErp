package com.hongs.hongs_erp.posheet.application.service;

import com.hongs.hongs_erp.posheet.application.port.in.AdminPoSheetUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrashPurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrashPurgeScheduler.class);
    private final AdminPoSheetUseCase adminUseCase;

    public TrashPurgeScheduler(AdminPoSheetUseCase adminUseCase) {
        this.adminUseCase = adminUseCase;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purge() {
        int count = adminUseCase.purgeExpired();
        log.info("posheet 휴지통 퍼지 완료: {}건", count);
    }
}
