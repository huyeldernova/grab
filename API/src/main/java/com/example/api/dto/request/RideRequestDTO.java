package com.example.api.dto.request;


import com.example.api.common.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RideRequestDTO {

    @NotNull
    private BigDecimal pickupLat;

    @NotNull
    private BigDecimal pickupLng;

    @NotBlank
    private String pickupAddress;

    @NotNull
    private BigDecimal dropoffLat;

    @NotNull
    private BigDecimal dropoffLng;

    @NotBlank
    private String dropoffAddress;

    @NotNull
    private VehicleType vehicleType;
}