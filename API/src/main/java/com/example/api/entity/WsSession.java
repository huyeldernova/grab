package com.example.api.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RedisHash("ws_session")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WsSession {

    @Id
    private String sessionId;

    @Indexed
    private String userId;

    private String role;

    @Builder.Default
    private LocalDateTime connectedAt = LocalDateTime.now();

    @TimeToLive(unit = TimeUnit.HOURS)
    @Builder.Default
    private Long ttl = 24L;
}