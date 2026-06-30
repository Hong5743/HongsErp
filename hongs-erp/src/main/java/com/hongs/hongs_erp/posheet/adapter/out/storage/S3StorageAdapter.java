package com.hongs.hongs_erp.posheet.adapter.out.storage;

import com.hongs.hongs_erp.posheet.application.port.out.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Component
public class S3StorageAdapter implements StoragePort {
    private final S3Client s3;
    private final S3Presigner presigner;
    @Value("${posheet.storage.bucket}") private String bucket;

    public S3StorageAdapter(S3Client s3, S3Presigner presigner) {
        this.s3 = s3; this.presigner = presigner;
    }

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(contentType).contentLength(size).build(),
                RequestBody.fromInputStream(data, size));
        return key;
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiry) {
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(r -> r.bucket(bucket).key(key).build())
                .build()).url().toString();
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public void deleteBatch(List<String> keys) {
        if (keys.isEmpty()) return;
        s3.deleteObjects(DeleteObjectsRequest.builder().bucket(bucket)
                .delete(Delete.builder()
                        .objects(keys.stream().map(k -> ObjectIdentifier.builder().key(k).build()).toList())
                        .build())
                .build());
    }
}
