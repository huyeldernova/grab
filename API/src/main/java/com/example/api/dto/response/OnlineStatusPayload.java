package com.example.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OnlineStatusPayload {
    private String userId;
    private String role;
    private boolean online;
}
