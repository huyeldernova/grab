package com.example.api.repository.jpa;

import com.example.api.entity.Ride;
import com.example.api.common.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    Optional<Ride> findByRequestId(Long requestId);
    List<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status);
}