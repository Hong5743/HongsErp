package com.hongs.hongs_erp.auth.application.port.out;

public interface LoginFailPort {
    int incrementAndGet(Long userId);
    void reset(Long userId);
}
