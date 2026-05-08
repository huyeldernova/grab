package com.example.api.controller;

import com.example.api.dto.response.ApiResponse;
import com.example.api.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // Driver cập nhật vị trí GPS → Redis GEO
    // Chỉ DRIVER mới được gọi
    @PreAuthorize("hasAuthority('DRIVER')")
    @PostMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Double lat,
            @RequestParam Double lng) {

        Long driverId = Long.parseLong(jwt.getSubject());
        driverService.updateLocation(driverId, lat, lng);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Location updated")
                .build());
    }

    // Driver bật nhận chuyến
    // → set isOnline = true trong DB
    @PreAuthorize("hasAuthority('DRIVER')")
    @PostMapping("/online")
    public ResponseEntity<ApiResponse<Void>> goOnline(
            @AuthenticationPrincipal Jwt jwt) {

        Long driverId = Long.parseLong(jwt.getSubject());
        driverService.goOnline(driverId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Driver is now online")
                .build());
    }

    // Driver tắt nhận chuyến
    // → set isOnline = false + xóa vị trí khỏi Redis GEO
    @PreAuthorize("hasAuthority('DRIVER')")
    @PostMapping("/offline")
    public ResponseEntity<ApiResponse<Void>> goOffline(
            @AuthenticationPrincipal Jwt jwt) {

        Long driverId = Long.parseLong(jwt.getSubject());
        driverService.goOffline(driverId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Driver is now offline")
                .build());
    }
}