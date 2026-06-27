package com.hongs.hongs_erp.auth.application.port.out;

public interface TokenBlacklistPort {
    void blacklist(String jti, long ttlSeconds);
    boolean isBlacklisted(String jti);
}
