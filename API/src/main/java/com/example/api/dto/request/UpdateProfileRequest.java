package com.example.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
}
