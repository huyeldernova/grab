package com.example.api.config;

import com.example.api.dto.response.OnlineStatusPayload;
import com.example.api.entity.WsSession;
import com.example.api.repository.jpa.DriverProfileRepository;
import com.example.api.repository.redis.WsSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final DriverProfileRepository driverProfileRepository;
    private final WsSessionRepository wsSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    @Transactional
    public void onConnect(SessionConnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap((Message<?>) event.getSource());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : null;

        if (userId == null) return;

        String role = "CUSTOMER";
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {

            role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.equals("DRIVER") || a.equals("ADMIN"))
                    .findFirst()
                    .orElse("CUSTOMER");
        }

        // Lưu session vào Redis
        wsSessionRepository.save(WsSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(role)
                .build());

        // Nếu là DRIVER → update is_online = true trong DB
        if ("DRIVER".equals(role)) {
            driverProfileRepository.findById(Long.parseLong(userId))
                    .ifPresent(driver -> {
                        driver.setOnline(true);
                        driverProfileRepository.save(driver);
                    });
        }

        // Broadcast online status cho admin theo dõi
        messagingTemplate.convertAndSend("/topic/online-status",
                OnlineStatusPayload.builder()
                        .userId(userId)
                        .role(role)
                        .online(true)
                        .build());

        log.info("WS connect: userId={}, role={}", userId, role);

    }

    @EventListener
    @Transactional
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : null;

        if (userId == null) return;

        // Xóa session này
        wsSessionRepository.deleteById(sessionId);

        // Kiểm tra user còn session nào khác không (multi-tab)
        List<WsSession> remaining = wsSessionRepository.findByUserId(userId);

        if (remaining.isEmpty()) {
            // Không còn session nào → thật sự offline

            // Tìm role từ session vừa xóa — dùng remaining trước khi delete
            // Nếu là DRIVER → update is_online = false
            driverProfileRepository.findById(Long.parseLong(userId))
                    .ifPresent(driver -> {
                        driver.setOnline(false);
                        driverProfileRepository.save(driver);
                    });

            // Broadcast offline status
            messagingTemplate.convertAndSend("/topic/online-status",
                    OnlineStatusPayload.builder()
                            .userId(userId)
                            .online(false)
                            .build());

            log.info("WS disconnect: userId={} is now offline", userId);
        } else {
            log.info("WS disconnect: userId={} still has {} sessions", userId, remaining.size());
        }
    }

}
