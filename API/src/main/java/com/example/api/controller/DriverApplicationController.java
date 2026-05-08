package com.example.api.controller;

import com.example.api.dto.request.DriverApplicationRequest;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.DriverApplicationResponse;
import com.example.api.service.DriverApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/driver-applications")
@RequiredArgsConstructor
public class DriverApplicationController {

    private final DriverApplicationService driverApplicationService;

    // User nộp đơn xin làm driver
    @PostMapping
    public ResponseEntity<ApiResponse<DriverApplicationResponse>> apply(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid DriverApplicationRequest request) {

        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<DriverApplicationResponse>builder()
                .code(200)
                .message("Application submitted successfully")
                .data(driverApplicationService.apply(userId, request))
                .build());
    }

    // User xem trạng thái đơn của mình
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DriverApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<List<DriverApplicationResponse>>builder()
                .code(200)
                .message("Applications retrieved")
                .data(driverApplicationService.getMyApplications(userId))
                .build());
    }
}