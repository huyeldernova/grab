package com.example.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UploadResult {
    private String s3Key;
    private String url;
}
