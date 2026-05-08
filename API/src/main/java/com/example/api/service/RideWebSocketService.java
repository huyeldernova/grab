package com.example.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // ── 1. Offer chuyến mới tới driver ────────────────────────
    public void notifyDriverRideOffer(Long driverId, Long requestId, String pickupAddress, String dropoffAddress) {
        // TODO 1: tạo payload map chứa: type, requestId, pickupAddress, dropoffAddress
        //         Gợi ý: dùng Map.of(...)
        Map<String, Object> payload = Map.of(
                "type", "RIDE_OFFER",
                "requestId", requestId,
                "pickupAddress", pickupAddress,
                "dropoffAddress", dropoffAddress
                );

        // TODO 2: push tới driver
        //         convertAndSendToUser(???, "/queue/ride-offer", payload)
        //         driverId phải là String vì Spring dùng getName() — kiểu String
        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverId),
                "/queue/ride-offer",
                payload
        );
    }

    // ── 2. Notify customer driver đã nhận ─────────────────────
    public void notifyCustomerRideMatched(Long customerId, Long rideId, Long driverId) {
        // TODO 3: tạo payload: type, rideId, driverId
        Map<String, Object> payload = Map.of(
                "type", "RIDE_MATCHED",
                "rideId", rideId,
                "driverId", driverId
                );

                // TODO 4: push tới customer /queue/ride-status
        messagingTemplate.convertAndSendToUser(
                String.valueOf(customerId),
                "/queue/ride-status",
                payload
        );
    }

    // ── 3. Notify customer status thay đổi ────────────────────
    public void notifyCustomerStatusUpdate(Long customerId, Long rideId, String newStatus) {
        // TODO 5: tạo payload: type, rideId, newStatus
        Map<String, Object> payload = Map.of(
                "type", "RIDE_STATUS_UPDATE",
                "rideId", rideId,
                "newStatus", newStatus
        );

        // TODO 6: push tới customer /queue/ride-status
        messagingTemplate.convertAndSendToUser(
                String.valueOf(customerId),
                "/queue/ride-status",
                payload
        );
    }

    // ── 4. Notify driver bị timeout (chuyến đã hết) ───────────
    public void notifyDriverOfferExpired(Long driverId, Long requestId) {
        // TODO 7: push tới driver /queue/ride-offer với type=OFFER_EXPIRED

        Map<String, Object> payload = Map.of(
                "type", "OFFER_EXPIRED",
                "requestId", requestId
        );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(driverId),
                "/queue/ride-offer",
                payload
        );

    }

}
