package com.example.api.dto.response;

import com.example.api.common.RideStatus;
import com.example.api.common.VehicleType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class RideRequestResponse {
    private Long id;
    private Long customerId;
    private String pickupAddress;
    private BigDecimal pickupLat;
    private BigDecimal pickupLng;
    private String dropoffAddress;
    private BigDecimal dropoffLat;
    private BigDecimal dropoffLng;
    private VehicleType vehicleType;
    private RideStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
