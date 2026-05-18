package com.example.api.dto.response;

import com.example.api.common.FileStatus;
import com.example.api.common.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponse {
    private Long id;
    private String url;
    private TargetType targetType;
    private Long targetId;
    private FileStatus status;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
}
