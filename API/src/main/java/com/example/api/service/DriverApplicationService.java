package com.example.api.service;

import com.example.api.common.ApplicationStatus;
import com.example.api.dto.request.DriverApplicationRequest;
import com.example.api.dto.response.DriverApplicationResponse;
import com.example.api.entity.DriverApplication;
import com.example.api.entity.DriverProfile;
import com.example.api.entity.Role;
import com.example.api.entity.User;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.jpa.DriverApplicationRepository;
import com.example.api.repository.jpa.DriverProfileRepository;
import com.example.api.repository.jpa.RoleRepository;
import com.example.api.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.example.api.common.AppConstant.DRIVER_ROLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverApplicationService {

    private final DriverApplicationRepository applicationRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;

    // ── 1. User nộp đơn ───────────────────────────────────────
    @Transactional
    public DriverApplicationResponse apply(Long userId, DriverApplicationRequest request) {

        // TODO 1: load User, throw USER_NOT_FOUND nếu không có
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // TODO 2: check user đã có đơn PENDING chưa
        //         Gợi ý: existsByUserIdAndStatus(userId, PENDING)
        //         Nếu có → throw exception gì?
        if(applicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new AppException(ErrorCode.DRIVER_APPLICATION_ALREADY_EXISTS);
        }


        // TODO 3: tạo DriverApplication entity từ request + user
        //         status mặc định = PENDING
        DriverApplication application = DriverApplication.builder()
                .user(user)
                .fullName(request.getFullName())
                .licenseNumber(request.getLicenseNumber())
                .vehicleType(request.getVehicleType())
                .plateNumber(request.getPlateNumber())
                .brand(request.getBrand())
                .model(request.getModel())
                .color(request.getColor())
                .build();

        // TODO 4: save và return mapToResponse(application)
        return mapToResponse(applicationRepository.save(application));
    }

    // ── 2. Admin approve ──────────────────────────────────────
    @Transactional
    public void approve(Long applicationId) {

        // TODO 5: load DriverApplication, throw nếu không có
        DriverApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_APPLICATION_NOT_FOUND));

        // TODO 6: check status == PENDING
        //         Nếu không → throw (không approve đơn đã xử lý)
        if(application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.DRIVER_APPLICATION_NOT_PENDING);
        }

        // TODO 7: tạo DriverProfile từ application
        //         Gợi ý: dùng application.getUser() để lấy user

        DriverProfile driverProfile = DriverProfile.builder()
                .id(application.getUser().getId())
                .user(application.getUser())
                .fullName(application.getFullName())
                .rating(BigDecimal.valueOf(5.0)) // rating mặc định 5 sao
                .build();
        driverProfileRepository.save(driverProfile);

        // TODO 8: assign role DRIVER cho user
        //         Gợi ý: nhìn lại UserService.createUser()
        //         dùng roleRepository.findByName("DRIVER")
        //         rồi user.addRole(role)
        //         rồi userRepository.save(user)
        Role driverRole = roleRepository.findByName(DRIVER_ROLE)
                .orElseThrow(() -> new IllegalStateException("DRIVER role not found"));
        application.getUser().addRole(driverRole);
        userRepository.save(application.getUser());

        // TODO 9: set application.status = APPROVED, save
        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        // TODO 10: gửi email thông báo
        //          mailService.sendApprovalEmail(user.getEmail())
        //          (method này chưa có, sẽ thêm sau)
        mailService.sendApprovalEmail(application.getUser().getEmail());
    }

    // ── 3. Admin reject ───────────────────────────────────────
    @Transactional
    public void reject(Long applicationId, String reason) {

        // TODO 11: load DriverApplication
        DriverApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_APPLICATION_NOT_FOUND));

        // TODO 12: check status == PENDING
        if(application.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.DRIVER_APPLICATION_NOT_PENDING);
        }

        // TODO 13: set status = REJECTED, rejectReason = reason, save
        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectReason(reason);
        applicationRepository.save(application);

        // TODO 14: gửi email thông báo từ chối
        mailService.sendRejectionEmail(application.getUser().getEmail(), reason);
    }

    // User xem đơn của mình
    public List<DriverApplicationResponse> getMyApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Admin xem tất cả đơn PENDING
    public List<DriverApplicationResponse> getApplications(ApplicationStatus status) {
        List<DriverApplication> list = status != null
                ? applicationRepository.findByStatus(status)
                : applicationRepository.findAll();
        return list.stream().map(this::mapToResponse).toList();
    }

    // ── Mapper ─────────────────────────────────────────────────
    private DriverApplicationResponse mapToResponse(DriverApplication app) {
        return DriverApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .fullName(app.getFullName())
                .licenseNumber(app.getLicenseNumber())
                .vehicleType(app.getVehicleType())
                .plateNumber(app.getPlateNumber())
                .brand(app.getBrand())
                .model(app.getModel())
                .color(app.getColor())
                .status(app.getStatus())
                .rejectReason(app.getRejectReason())
                .createdAt(app.getCreatedAt())
                .build();
    }
}


