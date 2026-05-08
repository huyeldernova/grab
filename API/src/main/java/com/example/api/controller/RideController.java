package com.example.api.controller;

import com.example.api.common.RideStatus;
import com.example.api.dto.request.RideRequestDTO;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.RideRequestResponse;
import com.example.api.dto.response.RideResponse;
import com.example.api.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    // Customer đặt xe
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RideRequestResponse>> createRequest(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid RideRequestDTO dto) {
        Long customerId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<RideRequestResponse>builder()
                .code(200)
                .message("Ride request created")
                .data(rideService.mapToRideRequestResponse(
                        rideService.createRequest(customerId, dto)))
                .build());
    }

    // Driver nhận chuyến
    @PreAuthorize("hasAuthority('DRIVER')")
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long requestId) {
        Long driverId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<RideResponse>builder()
                .code(200)
                .message("Ride accepted")
                .data(rideService.mapToRideResponse(
                        rideService.acceptRide(driverId, requestId)))
                .build());
    }

    // Driver cập nhật trạng thái
    @PreAuthorize("hasAuthority('DRIVER')")
    @PatchMapping("/{rideId}/status")
    public ResponseEntity<ApiResponse<RideResponse>> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long rideId,
            @RequestParam RideStatus status) {
        Long driverId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<RideResponse>builder()
                .code(200)
                .message("Status updated")
                .data(rideService.mapToRideResponse(
                        rideService.updateStatus(driverId, rideId, status)))
                .build());
    }

    // Customer hoặc Driver huỷ chuyến
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'DRIVER')")
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelRide(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long rideId,
            @RequestParam String reason) {
        Long userId = Long.parseLong(jwt.getSubject());
        rideService.cancelRide(userId, rideId, reason);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Ride cancelled")
                .build());
    }
}