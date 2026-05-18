package com.example.api.service;

import com.example.api.config.S3Properties;
import com.example.api.dto.response.S3UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "S3-SERVICE")
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    // ── Upload ────────────────────────────────────────────────
    public S3UploadResult upload(MultipartFile file, String folder) throws IOException {
        // TODO 1: tạo s3Key
        String extension = extractExtension(file.getOriginalFilename());
        String s3Key = "%s/%s%s".formatted(
                folder,
                UUID.randomUUID().toString(),
                extension
        );

        // TODO 2: tạo PutObjectRequest
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // TODO 3: upload lên S3
        s3Client.putObject(request, RequestBody.fromInputStream(
                file.getInputStream(),
                file.getSize()
                ));
        log.info("Uploaded to S3: {}", s3Key);

        // TODO 4: build URL và return S3UploadResult
        String url = "https://%s.s3.%s.amazonaws.com/%s".formatted(
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                s3Key
        );

        return S3UploadResult.builder()
                .s3Key(s3Key)
                .url(url)
                .build();
    }

    // ── Delete ────────────────────────────────────────────────
    public void delete(String s3Key) {
        s3Client.deleteObject(b -> b
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .build()
        );
        log.info("Deleted from S3: {}", s3Key);
    }

    // ── Helper ───────────────────────────────────────────────
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
