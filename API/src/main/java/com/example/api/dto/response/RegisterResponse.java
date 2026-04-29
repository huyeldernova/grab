package com.example.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private String email;
    private String username;
    private String message;
}