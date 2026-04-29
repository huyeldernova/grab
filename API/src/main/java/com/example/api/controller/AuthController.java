package com.example.api.controller;

import com.example.api.dto.request.CreateUserRequest;
import com.example.api.dto.request.LoginRequest;
import com.example.api.dto.request.ResendOtpRequest;
import com.example.api.dto.request.VerifyOtpRequest;
import com.example.api.dto.response.ApiResponse;
import com.example.api.dto.response.LoginResponse;
import com.example.api.dto.response.RegisterResponse;
import com.example.api.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.<RegisterResponse>builder()
                .code(200)
                .message("Registration successful. Please check your email for OTP.")
                .data(authenticationService.register(request))
                .build());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody @Valid VerifyOtpRequest request) {
        authenticationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Email verified successfully")
                .build());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody @Valid ResendOtpRequest request) {
        authenticationService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("OTP sent successfully")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("Login successful")
                .data(authenticationService.login(request))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) throws ParseException {
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build());
    }
}