package com.example.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VerifyOtpRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String otp;
}