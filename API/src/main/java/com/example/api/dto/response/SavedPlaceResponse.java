package com.example.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SavedPlaceResponse {
    private Long id;
    private String label;
    private String addressText;
    private String ward;
    private String district;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isDefault;
    private LocalDateTime createdAt;

}
