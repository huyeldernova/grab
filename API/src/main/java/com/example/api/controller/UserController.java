package com.example.api.controller;

import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.UserProfileResponse;
import com.example.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(@AuthenticationPrincipal Jwt jwt){

        Long userId = Long.parseLong(jwt.getSubject());

        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                        .code(200)
                        .message("get info successfully")
                        .data(userService.getMe(userId))
                        .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> search(@RequestParam("email") String email) {

        return ResponseEntity.ok(ApiResponse.<List<UserProfileResponse>>builder()
                .code(200)
                .message("get Profile successfully")
                .data(userService.getUserLikeByEmailOrUsername(email))
                .build());
    }

}
