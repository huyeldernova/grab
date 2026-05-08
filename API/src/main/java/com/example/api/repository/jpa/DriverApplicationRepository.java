package com.example.api.repository.jpa;

import com.example.api.common.ApplicationStatus;
import com.example.api.entity.DriverApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverApplicationRepository extends JpaRepository<DriverApplication, Long> {

    // Kiểm tra user đã có đơn PENDING chưa
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Admin xem danh sách đơn theo status
    List<DriverApplication> findByStatus(ApplicationStatus status);

    // User xem đơn của mình
    Optional<DriverApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Tìm tất cả đơn của 1 user, mới nhất trước
    List<DriverApplication> findByUserIdOrderByCreatedAtDesc(Long userId);
}