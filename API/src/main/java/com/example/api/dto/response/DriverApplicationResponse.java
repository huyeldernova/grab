package com.example.api.dto.response;

import com.example.api.common.ApplicationStatus;
import com.example.api.common.VehicleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DriverApplicationResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String licenseNumber;
    private VehicleType vehicleType;
    private String plateNumber;
    private String brand;
    private String model;
    private String color;
    private ApplicationStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;
}