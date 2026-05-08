package com.example.api.dto.request;

import com.example.api.common.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DriverApplicationRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    private String brand;
    private String model;
    private String color;
}