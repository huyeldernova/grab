package com.example.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "driver_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DriverProfile {

    @Id
    @Column(name = "user_id")
    private Long id;

    private String fullName;

    private String avatarUrl;

    @Column(nullable = false)
    private BigDecimal rating;

    @Builder.Default
    private boolean isOnline = false;

    private BigDecimal currentLat;

    private BigDecimal currentLng;

    // ── Document fields (nullable — driver chưa upload thì NULL) ──
    @Column(name = "cmnd_front_url", length = 1000)
    private String cmndFrontUrl;

    @Column(name = "cmnd_back_url", length = 1000)
    private String cmndBackUrl;

    @Column(name = "license_front_url", length = 1000)
    private String licenseFrontUrl;

    @Column(name = "license_back_url", length = 1000)
    private String licenseBackUrl;

    @Column(name = "vehicle_photo_url", length = 1000)
    private String vehiclePhotoUrl;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

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
