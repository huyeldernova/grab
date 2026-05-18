package com.example.api.controller;

import com.example.api.common.TargetType;
import com.example.api.dto.request.ActivateFilesRequest;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.FileResponse;
import com.example.api.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    // Upload nhiều file → trả List<FileResponse> với status=TEMP
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<FileResponse>>> upload(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam String folder) throws IOException {

        Long uploaderId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<List<FileResponse>>builder()
                .code(200)
                .message("Files uploaded successfully")
                .data(uploadService.uploadFiles(files, uploaderId, folder))
                .build());
    }

    // Activate files: TEMP → ACTIVE, gắn vào target
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ActivateFilesRequest request) {

        Long requesterId = Long.parseLong(jwt.getSubject());
        uploadService.activateFiles(
                request.getFileIds(),
                request.getTargetId(),
                request.getTargetType(),
                requesterId
        );
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Files activated successfully")
                .build());
    }

    // Lấy files theo target
    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFiles(
            @RequestParam TargetType targetType,
            @RequestParam Long targetId) {

        return ResponseEntity.ok(ApiResponse.<List<FileResponse>>builder()
                .code(200)
                .message("Files retrieved successfully")
                .data(uploadService.getFiles(targetType, targetId))
                .build());
    }

    // Xóa file
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Long requesterId = Long.parseLong(jwt.getSubject());
        uploadService.deleteFile(id, requesterId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("File deleted successfully")
                .build());
    }
}
