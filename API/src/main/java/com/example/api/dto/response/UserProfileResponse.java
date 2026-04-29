package com.example.api.dto.response;

import lombok.Builder;
import lombok.Getter;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;

    private String email;

    private String username;

    private String  phone;

    private String status;

    private List<String> roles;

    private boolean emailVerified;

    private LocalDateTime createdAt;
}
