package com.example.api.entity;

import com.example.api.common.RideStatus;
import com.example.api.common.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerProfile customerProfile;

    @Column(nullable = false, precision = 10, scale = 8)
    private java.math.BigDecimal pickupLat;

    @Column(nullable = false, precision = 11, scale = 8)
    private java.math.BigDecimal pickupLng;

    @Column(nullable = false, length = 500)
    private String pickupAddress;

    @Column(nullable = false, precision = 10, scale = 8)
    private java.math.BigDecimal dropoffLat;

    @Column(nullable = false, precision = 11, scale = 8)
    private java.math.BigDecimal dropoffLng;

    @Column(nullable = false, length = 500)
    private String dropoffAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RideStatus status = RideStatus.SEARCHING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiredAt == null) {
            this.expiredAt = this.createdAt.plusMinutes(2);
        }
    }
}