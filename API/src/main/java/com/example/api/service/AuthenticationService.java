package com.example.api.service;

import com.example.api.common.UserStatus;
import com.example.api.dto.request.CreateUserRequest;
import com.example.api.dto.request.LoginRequest;
import com.example.api.dto.request.ResendOtpRequest;
import com.example.api.dto.request.VerifyOtpRequest;
import com.example.api.dto.response.CreateUserResponse;
import com.example.api.dto.response.LoginResponse;
import com.example.api.dto.response.RegisterResponse;
import com.example.api.dto.token.TokenPayload;
import com.example.api.entity.RedisToken;
import com.example.api.entity.User;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.UserRepository;
import com.example.api.repository.redis.RedisTokenRepository;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTokenRepository redisTokenRepository;
    private final UserService userService;
    private final OtpService otpService;
    private final MailService mailService;
    private final UserRepository userRepository;

    // ── Register ──────────────────────────────────────────
    public RegisterResponse register(CreateUserRequest request) {
        CreateUserResponse created = userService.createUser(request);

        String otp = otpService.generateAndStore(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), otp);

        return RegisterResponse.builder()
                .email(created.getEmail())
                .username(created.getUsername())
                .message("Registration successful. Please check your email for OTP.")
                .build();
    }
    // ── Verify Email ───────────────────────────────────────
    public void verifyEmail(VerifyOtpRequest request) {
        otpService.verify(request.getEmail(), request.getOtp());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(UserStatus.valueOf("ACTIVE"));
        userRepository.save(user);
    }

    // ── Resend OTP ─────────────────────────────────────────
    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if ("ACTIVE".equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        otpService.checkResendRateLimit(request.getEmail());

        String otp = otpService.generateAndStore(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), otp);

        otpService.recordResend(request.getEmail());
    }

    // ── Login ──────────────────────────────────────────────
    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        User user = (User) authentication.getPrincipal();

        Set<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        TokenPayload payloadAccessToken = jwtService.generateAccessToken(user.getId(), authorities);
        TokenPayload payloadRefreshToken = jwtService.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(payloadAccessToken.getToken())
                .refreshToken(payloadRefreshToken.getToken())
                .build();
    }

    // ── Logout ─────────────────────────────────────────────
    public void logout(String accessToken) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        Date now = new Date();

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Long diff = expiredTime.getTime() - now.getTime();

        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expirationDate(diff)
                .build();

        redisTokenRepository.save(redisToken);
        log.info("Token saved to redis: {}", accessToken);
    }
}