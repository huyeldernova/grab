package com.example.api.dto.response;


import com.example.api.common.RideStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RideResponse {
    private Long id;
    private Long requestId;
    private Long driverId;
    private Long customerId;
    private RideStatus status;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private String dropoffAddress;
    private BigDecimal dropoffLat;
    private BigDecimal dropoffLng;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal fare;
    private String cancelledBy;
    private String cancelReason;
    private LocalDateTime createdAt;
}