package com.example.api.service;


import com.example.api.common.UserStatus;
import com.example.api.dto.request.CreateUserRequest;
import com.example.api.dto.response.CreateUserResponse;
import com.example.api.dto.response.UserDetailResponse;
import com.example.api.entity.Role;
import com.example.api.entity.User;
import com.example.api.exception.AppException;
import com.example.api.exception.ErrorCode;
import com.example.api.repository.RoleRepository;
import com.example.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.api.common.AppConstant.CUSTOMER_ROLE;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional(rollbackFor = Exception.class)
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.INACTIVE)
                .build();


        Role customerRole = roleRepository.findByName(CUSTOMER_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Role CUSTOMER not found in database. Did V2 migration run correctly?"));

        user.addRole(customerRole);

        userRepository.save(user);

        return CreateUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public List<UserDetailResponse> getUserLikeByEmailOrUsername(String emailOrUsername){
        var users = userRepository.searchByEmailOrUsername(emailOrUsername);

        return users.stream().map(u -> {
            var roles = u.getUserHasRoles().stream()
                    .map(ur -> ur.getRole().getName())
                    .toList();

            return UserDetailResponse.builder()
                    .id(u.getId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .roles(roles)
                    .build();
        }).toList();
    }

}