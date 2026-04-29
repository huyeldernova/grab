package com.example.api.controller;

import com.example.api.dto.request.SavedPlaceRequest;
import com.example.api.dto.request.UpdateProfileRequest;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.CustomerProfileResponse;
import com.example.api.dto.response.SavedPlaceResponse;
import com.example.api.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<CustomerProfileResponse>builder()
                .code(200)
                .message("Profile retrieved successfully")
                .data(customerProfileService.getProfile(userId))
                .build());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdateProfileRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<CustomerProfileResponse>builder()
                .code(200)
                .message("Profile updated successfully")
                .data(customerProfileService.updateProfile(userId, request))
                .build());
    }

    @GetMapping("/saved-places")
    public ResponseEntity<ApiResponse<List<SavedPlaceResponse>>> getSavedPlaces(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<List<SavedPlaceResponse>>builder()
                .code(200)
                .message("Saved places retrieved successfully")
                .data(customerProfileService.getSavedPlaces(userId))
                .build());
    }

    @PostMapping("/saved-places")
    public ResponseEntity<ApiResponse<SavedPlaceResponse>> createSavedPlace(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid SavedPlaceRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<SavedPlaceResponse>builder()
                .code(200)
                .message("Saved place created successfully")
                .data(customerProfileService.createSavedPlace(userId, request))
                .build());
    }

    @PutMapping("/saved-places/{id}")
    public ResponseEntity<ApiResponse<SavedPlaceResponse>> updateSavedPlace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestBody @Valid SavedPlaceRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.<SavedPlaceResponse>builder()
                .code(200)
                .message("Saved place updated successfully")
                .data(customerProfileService.updateSavedPlace(userId, id, request))
                .build());
    }

    @DeleteMapping("/saved-places/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedPlace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        customerProfileService.deleteSavedPlace(userId, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Saved place deleted successfully")
                .build());
    }
}