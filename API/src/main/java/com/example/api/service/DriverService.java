package com.example.api.service;

import com.example.api.entity.DriverProfile;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String GEO_KEY = "drivers:location";


    // ── 1. Driver update vị trí ───────────────────────────────
    public void updateLocation(Long driverId, Double lat, Double lng) {
        // TODO 1: kiểm tra driver có tồn tại không
        //         Gợi ý: existsById() — không cần load cả entity

        if(!driverProfileRepository.existsById(driverId)){
            throw new AppException(ErrorCode.PROFILE_NOT_FOUND);
        }

        // TODO 2: lưu vị trí vào Redis GEO
        //         opsForGeo().add(GEO_KEY, new Point(lng, lat), driverId.toString())
        //         Lưu ý: Point(longitude, latitude) — thứ tự lng trước lat
        redisTemplate.opsForGeo().add(
                GEO_KEY,
                new Point(lng, lat),
                driverId.toString()
        );

        // TODO 3: log vị trí mới
        log.info("Driver {} location updated: lat={}, lng={}", driverId, lat, lng);
    }

    // ── 2. Driver online ──────────────────────────────────────
    @Transactional
    public void goOnline(Long driverId) {
        // TODO 4: load DriverProfile, throw nếu không có
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        // TODO 5: set isOnline = true, save
        profile.setOnline(true);

        driverProfileRepository.save(profile);

    }

    // ── 3. Driver offline ─────────────────────────────────────
    @Transactional
    public void goOffline(Long driverId) {
        // TODO 6: load DriverProfile, throw nếu không có
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        // TODO 7: set isOnline = false, save
        profile.setOnline(false);
        driverProfileRepository.save(profile);
        // TODO 8: xóa vị trí khỏi Redis GEO
        //         opsForGeo().remove(GEO_KEY, driverId.toString())
        redisTemplate.opsForGeo().remove(GEO_KEY,driverId.toString());

    }

}
