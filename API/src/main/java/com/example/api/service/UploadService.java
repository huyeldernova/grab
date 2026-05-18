package com.example.api.service;

import com.example.api.common.FileStatus;
import com.example.api.common.TargetType;
import com.example.api.dto.response.FileResponse;
import com.example.api.dto.response.S3UploadResult;
import com.example.api.entity.FileRecord;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UPLOAD-SERVICE")
public class UploadService {

    private final S3Service s3Service;
    private final FileValidator fileValidator;
    private final FileRepository fileRepository;

    // ── Upload nhiều file ─────────────────────────────────────
    public List<FileResponse> uploadFiles(List<MultipartFile> files, Long uploaderId, String folder) throws IOException {
        // TODO 1: check list file có rỗng không
        if (files == null || files.isEmpty()) return new ArrayList<>();

        // TODO 2: loop từng file:
        //         - validate
        //         - upload S3 → S3UploadResult
        //         - nếu S3 thành công nhưng DB lỗi → xóa S3 lại (rollback)
        //         - lưu FileRecord(status=TEMP) vào DB
        //         - map sang FileResponse
        List<FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files){
            fileValidator.validate(file);
            S3UploadResult result = s3Service.upload(file, folder);

            try {
                FileRecord record = FileRecord.builder()
                        .uploaderId(uploaderId)
                        .s3Key(result.getS3Key())
                        .url(result.getUrl())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .status(FileStatus.TEMP)
                        .build();

                fileRepository.save(record);
                responses.add(mapToResponse(record));

            } catch (Exception e) {
                // S3 đã upload rồi nhưng DB lỗi → xóa S3 lại
                s3Service.delete(result.getS3Key());
                log.error("DB save failed, rolled back S3: {}", result.getS3Key());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        return responses;
        }

    @Transactional
    public void activateFiles(List<Long> fileIds, Long targetId, TargetType targetType, Long requesterId) {
        // TODO 3: load files theo fileIds + uploaderId (check ownership)
        //         Gợi ý: fileRepository.findByIdInAndUploaderId(fileIds, requesterId)
        List<FileRecord> records = fileRepository.findByIdInAndUploaderId(fileIds, requesterId);

        // TODO 4: check số lượng tìm được == số lượng request
        //         Gợi ý: nếu khác nhau → có file không tồn tại hoặc không thuộc về requesterId
        if (records.size() != fileIds.size()) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }


        // TODO 5: update từng record
        //         → status = ACTIVE
        //         → targetId = targetId
        //         → targetType = targetType
        records.forEach(record -> {
            record.setStatus(FileStatus.ACTIVE);
            record.setTargetId(targetId);
            record.setTargetType(targetType);
        });

        fileRepository.saveAll(records);
    }

    public List<FileResponse> getFiles(TargetType targetType, Long targetId) {
        return fileRepository
                .findByTargetTypeAndTargetId(targetType, targetId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteFile(Long fileId, Long requesterId) {
        FileRecord record = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        if (!record.getUploaderId().equals(requesterId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        s3Service.delete(record.getS3Key());
        fileRepository.delete(record);
    }

    private FileResponse mapToResponse(FileRecord record) {
        return FileResponse.builder()
                .id(record.getId())
                .url(record.getUrl())
                .targetType(record.getTargetType())
                .targetId(record.getTargetId())
                .status(record.getStatus())
                .contentType(record.getContentType())
                .size(record.getSize())
                .createdAt(record.getCreatedAt())
                .build();
    }


}

