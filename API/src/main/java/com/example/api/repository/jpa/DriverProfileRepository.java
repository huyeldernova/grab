package com.example.api.repository.jpa;

import com.example.api.entity.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE DriverProfile d SET d.currentLat = :lat, d.currentLng = :lng WHERE d.id = :driverId")
    void updateLocation(@Param("driverId") Long driverId,
                        @Param("lat") BigDecimal lat,
                        @Param("lng") BigDecimal lng);
}