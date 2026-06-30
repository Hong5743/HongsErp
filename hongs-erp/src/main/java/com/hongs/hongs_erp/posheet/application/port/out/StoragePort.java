package com.hongs.hongs_erp.posheet.application.port.out;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

public interface StoragePort {
    String upload(String key, InputStream data, long size, String contentType);
    String generatePresignedUrl(String key, Duration expiry);
    void delete(String key);
    void deleteBatch(List<String> keys);
}
