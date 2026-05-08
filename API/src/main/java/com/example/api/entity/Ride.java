package com.example.api.entity;

import com.example.api.common.RideStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private RideRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RideStatus status = RideStatus.MATCHED;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal pickupLat;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal pickupLng;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal dropoffLat;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal dropoffLng;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(precision = 10, scale = 2)
    private BigDecimal fare;

    @Column(length = 20)
    private String cancelledBy;

    @Column(length = 500)
    private String cancelReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}