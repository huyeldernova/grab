package com.example.api.dto.request;

import com.example.api.common.TargetType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ActivateFilesRequest {

    @NotEmpty(message = "File IDs are required")
    private List<Long> fileIds;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Target type is required")
    private TargetType targetType;
}