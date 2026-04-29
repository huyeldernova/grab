package com.example.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class    SavedPlaceRequest {

    @NotBlank(message = "Label is required")
    private String label;

    @NotBlank(message = "Address is required")
    private String addressText;

    private String ward;

    private String district;

    private String city;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private boolean isDefault;

}
