package com.example.api.service;

import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
public class OtpService {

    private final StringRedisTemplate redis;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_TTL_MINUTES = 10;
    private static final int LOCK_TTL_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 3;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int RESEND_HOURLY_MAX = 5;

    public String generateAndStore(String email) {
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(999999));
        redis.opsForValue().set("otp:code:" + email, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redis.delete("otp:attempts:" + email);
        log.info("[DEV] OTP for {}: {}", email, otp);
        return otp;
    }

    public void verify(String email, String inputOtp) {
        if (Boolean.TRUE.equals(redis.hasKey("otp:lock:" + email))) {
            throw new AppException(ErrorCode.OTP_LOCKED);
        }

        String storedOtp = redis.opsForValue().get("otp:code:" + email);
        if (storedOtp == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!storedOtp.equals(inputOtp)) {
            long attempts = incrementAttempts(email);
            if (attempts >= MAX_ATTEMPTS) {
                redis.opsForValue().set("otp:lock:" + email, "1", LOCK_TTL_MINUTES, TimeUnit.MINUTES);
                redis.delete("otp:code:" + email);
                throw new AppException(ErrorCode.OTP_LOCKED);
            }
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        redis.delete("otp:code:" + email);
        redis.delete("otp:attempts:" + email);
        redis.delete("otp:lock:" + email);
    }

    public void checkResendRateLimit(String email) {
        if (Boolean.TRUE.equals(redis.hasKey("otp:resend:cooldown:" + email))) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_FAST);
        }

        String hourlyStr = redis.opsForValue().get("otp:resend:hourly:" + email);
        int hourlyCount = hourlyStr == null ? 0 : Integer.parseInt(hourlyStr);
        if (hourlyCount >= RESEND_HOURLY_MAX) {
            throw new AppException(ErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
        }
    }

    public void recordResend(String email) {
        redis.opsForValue().set("otp:resend:cooldown:" + email, "1", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        redis.opsForValue().increment("otp:resend:hourly:" + email);
        redis.expire("otp:resend:hourly:" + email, 1, TimeUnit.HOURS);
    }

    private long incrementAttempts(String email) {
        Long attempts = redis.opsForValue().increment("otp:attempts:" + email);
        redis.expire("otp:attempts:" + email, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        return attempts == null ? 1 : attempts;
    }
}