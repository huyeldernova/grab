package com.example.api.dto.response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDetailResponse {
    private Long id;
    private String email;
    private String username;
    private List<String> roles;
}

