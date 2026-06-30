package com.hongs.hongs_erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI;

@Configuration
public class S3Config {
    @Value("${posheet.storage.endpoint:}") private String endpoint;
    @Value("${posheet.storage.access-key}") private String accessKey;
    @Value("${posheet.storage.secret-key}") private String secretKey;
    @Value("${posheet.storage.region:ap-northeast-2}") private String region;

    private StaticCredentialsProvider creds() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean
    public S3Client s3Client() {
        var b = S3Client.builder().credentialsProvider(creds()).region(Region.of(region));
        if (!endpoint.isBlank()) b.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        return b.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var b = S3Presigner.builder().credentialsProvider(creds()).region(Region.of(region));
        if (!endpoint.isBlank()) b.endpointOverride(URI.create(endpoint));
        return b.build();
    }
}
