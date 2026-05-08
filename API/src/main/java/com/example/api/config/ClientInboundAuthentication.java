package com.example.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CLIENT-INBOUND-AUTHENTICATION")
public class ClientInboundAuthentication implements ChannelInterceptor {

    private final JwtDecoderConfiguration jwtDecoder;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // Bước 1: Mở phong bì — lấy STOMP header reader
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);

        // Bước 2: Chỉ xử lý khi client lần đầu kết nối
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION);

            // Bước 3: Kiểm tra có token không
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // bỏ "Bearer "

                try {
                    // Bước 4: Quẹt thẻ — decode JWT
                    Jwt jwt = jwtDecoder.decode(token);
                    String userId = jwt.getSubject(); // lấy userId

                    // Bước 5: Lấy danh sách role (CUSTOMER/DRIVER/ADMIN)
                    List<SimpleGrantedAuthority> authorities = Optional
                            .ofNullable(jwt.getClaimAsStringList("authorities"))
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    // Bước 6: Tạo authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    // Bước 7: Dán badge lên session
                    // Spring dùng userId này để route /user/queue đúng người
                    accessor.setUser(authentication);
                    log.info("WS connected: userId={}", userId);

                } catch (Exception e) {
                    // Token hỏng/giả → đuổi ra
                    log.error("WS auth failed: {}", e.getMessage());
                    throw new MessagingException("Invalid token");
                }
            }
        }

        // Cho message đi tiếp vào hệ thống
        return message;
    }

}
