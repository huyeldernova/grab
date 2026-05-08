package com.example.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    private final ClientInboundAuthentication clientInboundAuthentication;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173"
                )
                .addInterceptors(webSocketHandshakeInterceptor);
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Server → Client
        registry.enableSimpleBroker("/topic", "/queue");
        // Client → Server
        registry.setApplicationDestinationPrefixes("/app");
        // Private channel
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(clientInboundAuthentication);
    }
}





































































