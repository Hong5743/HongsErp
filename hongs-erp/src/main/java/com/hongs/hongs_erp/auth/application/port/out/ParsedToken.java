package com.hongs.hongs_erp.auth.application.port.out;

public record ParsedToken(String tokenId, String subject, String role, long remainingSeconds) {}
