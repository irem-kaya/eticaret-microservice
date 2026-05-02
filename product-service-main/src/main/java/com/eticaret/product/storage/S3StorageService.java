package com.eticaret.product.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${app.storage.bucket}")
    private String bucket;

    @Value("${app.storage.region}")
    private String region;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            return getUrl(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Dosya yüklenemedi: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build());
    }

    @Override
    public String getUrl(String fileName) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}