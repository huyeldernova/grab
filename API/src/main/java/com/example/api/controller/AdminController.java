package com.example.api.controller;

import com.example.api.common.ApplicationStatus;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.DriverApplicationResponse;
import com.example.api.service.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final DriverApplicationService driverApplicationService;

    // Xem tất cả đơn PENDING
    @GetMapping("/driver-applications")
    public ResponseEntity<ApiResponse<List<DriverApplicationResponse>>> getApplications(
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(ApiResponse.<List<DriverApplicationResponse>>builder()
                .code(200)
                .message("Applications retrieved")
                .data(driverApplicationService.getApplications(status))
                .build());
    }
    // Duyệt đơn
    @PostMapping("/driver-applications/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        driverApplicationService.approve(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Application approved")
                .build());
    }

    // Từ chối đơn
    @PostMapping("/driver-applications/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @RequestParam String reason) {
        driverApplicationService.reject(id, reason);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Application rejected")
                .build());
    }
}