package com.example.api.service;

import com.example.api.common.FileStatus;
import com.example.api.entity.FileRecord;
import com.example.api.repository.jpa.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CLEANUP-JOB")
public class FileCleanupJob {

    private final FileRepository fileRepository;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupTempFiles() {
        // TODO 2: tìm TEMP files quá 24h
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<FileRecord> orphanedFiles = fileRepository.findByStatusAndCreatedAtBefore(FileStatus.TEMP, cutoff);

        // TODO 3: nếu không có file nào → log + return
        if (orphanedFiles.isEmpty()) {
            log.info("No orphaned files to clean up");
            return;
        }

        // TODO 4: loop từng file:
        //         - xóa S3
        //         - xóa DB record
        orphanedFiles.forEach(file -> {
            s3Service.delete(file.getS3Key());
            fileRepository.delete(file);
        });

    }
}