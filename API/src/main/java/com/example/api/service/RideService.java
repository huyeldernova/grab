package com.example.api.service;

import com.example.api.common.RideStatus;
import com.example.api.dto.request.RideRequestDTO;
import com.example.api.dto.response.RideRequestResponse;
import com.example.api.dto.response.RideResponse;
import com.example.api.entity.CustomerProfile;
import com.example.api.entity.DriverProfile;
import com.example.api.entity.Ride;
import com.example.api.entity.RideRequest;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.CustomerProfileRepository;
import com.example.api.repository.jpa.DriverProfileRepository;
import com.example.api.repository.jpa.RideRepository;
import com.example.api.repository.jpa.RideRequestRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRequestRepository rideRequestRepository;
    private final RideRepository rideRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final StringRedisTemplate redisTemplate;
    private final RideWebSocketService rideWebSocketService;

    // Thread pool để schedule timeout task sau mỗi offer
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4);

    // ── Constants ──────────────────────────────────────────────
    private static final long OFFER_TTL_SECONDS = 10;       // timeout mỗi offer
    private static final String GEO_KEY          = "drivers:location";
    private static final String OFFER_REQUEST_KEY = "offer:request:"; // request đang offer cho ai
    private static final String OFFER_DRIVER_KEY  = "offer:driver:";  // driver đang được offer gì
    private static final double[] SEARCH_RADII    = {5.0, 10.0, 15.0}; // progressive radius

    // ── State machine: định nghĩa transition hợp lệ ───────────
    private static final Map<RideStatus, List<RideStatus>> VALID_TRANSITIONS = Map.of(
            RideStatus.SEARCHING,      List.of(RideStatus.MATCHED, RideStatus.CANCELLED),
            RideStatus.MATCHED,        List.of(RideStatus.DRIVER_ARRIVED, RideStatus.CANCELLED),
            RideStatus.DRIVER_ARRIVED, List.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED),
            RideStatus.IN_PROGRESS,    List.of(RideStatus.COMPLETED),
            RideStatus.COMPLETED,      List.of(),
            RideStatus.CANCELLED,      List.of()
    );

    // ──────────────────────────────────────────────────────────
    // 1. Customer tạo ride request
    // ──────────────────────────────────────────────────────────
    @Transactional
    public RideRequest createRequest(Long customerId, RideRequestDTO dto) {

        // Kiểm tra customer profile tồn tại
        CustomerProfile profile = customerProfileRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        // Tạo RideRequest, status mặc định = SEARCHING
        RideRequest request = RideRequest.builder()
                .customerProfile(profile)
                .pickupLat(dto.getPickupLat())
                .pickupLng(dto.getPickupLng())
                .pickupAddress(dto.getPickupAddress())
                .dropoffLat(dto.getDropoffLat())
                .dropoffLng(dto.getDropoffLng())
                .dropoffAddress(dto.getDropoffAddress())
                .vehicleType(dto.getVehicleType())
                .build();

        RideRequest saved = rideRequestRepository.save(request);

        // Bắt đầu tìm driver ngay, radius đầu tiên = 5km
        offerNextDriver(saved.getId(), null, SEARCH_RADII[0]);

        return saved;
    }

    // ──────────────────────────────────────────────────────────
    // 2. Tìm và offer driver tiếp theo (đệ quy)
    // ──────────────────────────────────────────────────────────
    @Transactional
    public void offerNextDriver(Long requestId, Long excludeDriverId, double radiusKm) {

        // Load request — nếu không còn SEARCHING thì dừng
        RideRequest request = rideRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RIDE_REQUEST_NOT_FOUND));

        if (LocalDateTime.now().isAfter(request.getExpiredAt())) {
            request.setStatus(RideStatus.EXPIRED);
            rideRequestRepository.save(request);
            log.info("Request {} expired at {}", requestId, request.getExpiredAt());
            return;
        }

        if (request.getStatus() != RideStatus.SEARCHING) return;

        // Query Redis GEO — tìm tối đa 10 driver trong bán kính, gần nhất trước
        var results = redisTemplate.opsForGeo().radius(GEO_KEY,
                new Circle(
                        new Point(request.getPickupLng().doubleValue(),
                                request.getPickupLat().doubleValue()),
                        new Distance(radiusKm, Metrics.KILOMETERS)
                ),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .sortAscending().limit(10)
        );

        // Filter: bỏ excludeDriverId (vừa từ chối/timeout) và driver đang bận
        Long selectedDriverId = null;
        if (results != null) {
            selectedDriverId = results.getContent().stream()
                    .map(r -> Long.parseLong(r.getContent().getName()))
                    .filter(id -> !id.equals(excludeDriverId))
                    .filter(id -> !Boolean.TRUE.equals(
                            redisTemplate.hasKey(OFFER_DRIVER_KEY + id)))
                    .findFirst()
                    .orElse(null);
        }

        // Không tìm được driver → thử radius lớn hơn hoặc EXPIRED
        if (selectedDriverId == null) {
            double nextRadius = nextRadius(radiusKm);
            if (nextRadius == -1) {
                // Đã thử hết 5→10→15km → EXPIRED
                if (excludeDriverId != null) {
                    rideWebSocketService.notifyDriverOfferExpired(excludeDriverId, requestId);
                }
                request.setStatus(RideStatus.EXPIRED);
                rideRequestRepository.save(request);
                return;
            }
            // Mở rộng radius, gọi lại ngay lập tức
            offerNextDriver(requestId, excludeDriverId, nextRadius);
            return;
        }

        // Claim driver bằng SETNX — tránh race condition khi 2 request cùng chọn 1 driver
        Boolean claimed = redisTemplate.opsForValue().setIfAbsent(
                OFFER_DRIVER_KEY + selectedDriverId,
                requestId.toString(),
                OFFER_TTL_SECONDS, TimeUnit.SECONDS
        );

        if (!Boolean.TRUE.equals(claimed)) {
            // Driver vừa bị request khác claim → thử driver tiếp
            offerNextDriver(requestId, selectedDriverId, radiusKm);
            return;
        }

        // Lưu offer state: request này đang offer cho driver nào
        redisTemplate.opsForValue().set(
                OFFER_REQUEST_KEY + requestId,
                selectedDriverId.toString(),
                OFFER_TTL_SECONDS, TimeUnit.SECONDS
        );

        // Notify driver qua WebSocket
        rideWebSocketService.notifyDriverRideOffer(
                selectedDriverId, requestId,
                request.getPickupAddress(), request.getDropoffAddress()
        );

        // Schedule timeout: sau 10s nếu driver không nhận → offer người tiếp
        final Long finalDriverId = selectedDriverId;
        scheduler.schedule(() -> {
            try {
                offerNextDriver(requestId, finalDriverId, radiusKm);
            } catch (Exception e) {
                log.error("offerNextDriver failed for requestId={}: {}",
                        requestId, e.getMessage(), e);
            }
        }, OFFER_TTL_SECONDS, TimeUnit.SECONDS);
    }

    // ──────────────────────────────────────────────────────────
    // 3. Driver nhận chuyến
    // ──────────────────────────────────────────────────────────
    @Transactional
    public Ride acceptRide(Long driverId, Long requestId) {

        // Load request
        RideRequest request = rideRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RIDE_REQUEST_NOT_FOUND));

        // SETNX claim — chỉ 1 driver thắng khi nhiều driver cùng accept
        if (!Boolean.TRUE.equals(claimRideRequest(requestId, driverId))) {
            throw new AppException(ErrorCode.RIDE_REQUEST_NOT_AVAILABLE);
        }

        // Double-check status vẫn còn SEARCHING
        if (request.getStatus() != RideStatus.SEARCHING) {
            throw new AppException(ErrorCode.RIDE_REQUEST_NOT_AVAILABLE);
        }

        // Load driver profile
        DriverProfile profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        // Tạo Ride entity — copy pickup/dropoff từ request
        Ride ride = Ride.builder()
                .request(request)
                .driver(profile)
                .customer(request.getCustomerProfile())
                .status(RideStatus.MATCHED)
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .build();

        // Update request status → MATCHED
        request.setStatus(RideStatus.MATCHED);

        // Xóa offer keys — driver và request không còn bị "giữ" trong Redis
        redisTemplate.delete(OFFER_REQUEST_KEY + requestId);
        redisTemplate.delete(OFFER_DRIVER_KEY + driverId);

        rideRepository.save(ride);
        rideRequestRepository.save(request);

        // Notify customer qua WebSocket
        rideWebSocketService.notifyCustomerRideMatched(
                ride.getCustomer().getId(), ride.getId(), driverId);

        return ride;
    }

    // ──────────────────────────────────────────────────────────
    // 4. Driver cập nhật trạng thái chuyến
    // ──────────────────────────────────────────────────────────
    @Transactional
    public Ride updateStatus(Long driverId, Long rideId, RideStatus newStatus) {

        // Load ride
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new AppException(ErrorCode.RIDE_NOT_FOUND));

        // Chỉ driver của chuyến này mới được cập nhật
        if (!ride.getDriver().getId().equals(driverId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Kiểm tra transition hợp lệ theo state machine
        validateTransition(ride.getStatus(), newStatus);

        // Cập nhật trạng thái + timestamp
        ride.setStatus(newStatus);
        if (newStatus == RideStatus.IN_PROGRESS) ride.setStartTime(LocalDateTime.now());
        if (newStatus == RideStatus.COMPLETED)   ride.setEndTime(LocalDateTime.now());

        Ride saved = rideRepository.save(ride);

        // Notify customer biết trạng thái mới
        rideWebSocketService.notifyCustomerStatusUpdate(
                ride.getCustomer().getId(), ride.getId(), newStatus.name());

        return saved;
    }

    // ──────────────────────────────────────────────────────────
    // 5. Huỷ chuyến (Customer hoặc Driver)
    // ──────────────────────────────────────────────────────────
    @Transactional
    public void cancelRide(Long userId, Long rideId, String reason) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new AppException(ErrorCode.RIDE_NOT_FOUND));

        // validateTransition tự check: chỉ MATCHED/DRIVER_ARRIVED được huỷ
        validateTransition(ride.getStatus(), RideStatus.CANCELLED);

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledBy(userId.toString());
        ride.setCancelReason(reason);
        rideRepository.save(ride);

        // Notify customer biết chuyến đã bị huỷ
        rideWebSocketService.notifyCustomerStatusUpdate(
                ride.getCustomer().getId(), ride.getId(), RideStatus.CANCELLED.name());
    }

    public List<RideResponse> getCustomerHistory(Long customerId) {
        return rideRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToRideResponse)
                .toList();
    }

    public List<RideResponse> getDriverHistory(Long driverId) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(this::mapToRideResponse)
                .toList();
    }

    // ──────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────

    // SETNX: chỉ 1 driver claim được 1 request tại 1 thời điểm
    private boolean claimRideRequest(Long requestId, Long driverId) {
        String key = "ride:claim:" + requestId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, driverId.toString(), OFFER_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    // Kiểm tra transition có hợp lệ không, throw nếu sai
    private void validateTransition(RideStatus current, RideStatus next) {
        List<RideStatus> allowed = VALID_TRANSITIONS.get(current);
        if (!allowed.contains(next)) {
            throw new AppException(ErrorCode.INVALID_RIDE_STATUS_TRANSITION);
        }
    }

    // Trả về radius tiếp theo trong mảng SEARCH_RADII, -1 nếu đã hết
    private double nextRadius(double current) {
        for (int i = 0; i < SEARCH_RADII.length - 1; i++) {
            if (SEARCH_RADII[i] == current) return SEARCH_RADII[i + 1];
        }
        return -1;
    }

    // Shutdown scheduler gracefully khi Spring context đóng
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ride scheduler...");
        scheduler.shutdown();
    }

    // ──────────────────────────────────────────────────────────
    // Mappers: Entity → DTO (tránh expose entity ra ngoài)
    // ──────────────────────────────────────────────────────────

    public RideRequestResponse mapToRideRequestResponse(RideRequest request) {
        return RideRequestResponse.builder()
                .id(request.getId())
                .customerId(request.getCustomerProfile().getId())
                .pickupAddress(request.getPickupAddress())
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .dropoffAddress(request.getDropoffAddress())
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .vehicleType(request.getVehicleType())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .expiredAt(request.getExpiredAt())
                .build();
    }

    public RideResponse mapToRideResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .requestId(ride.getRequest().getId())
                .driverId(ride.getDriver().getId())
                .customerId(ride.getCustomer().getId())
                .status(ride.getStatus())
                .pickupAddress(ride.getRequest().getPickupAddress())
                .pickupLat(ride.getPickupLat())
                .pickupLng(ride.getPickupLng())
                .dropoffAddress(ride.getRequest().getDropoffAddress())
                .dropoffLat(ride.getDropoffLat())
                .dropoffLng(ride.getDropoffLng())
                .startTime(ride.getStartTime())
                .endTime(ride.getEndTime())
                .fare(ride.getFare())
                .cancelledBy(ride.getCancelledBy())
                .cancelReason(ride.getCancelReason())
                .createdAt(ride.getCreatedAt())
                .build();
    }
}