package com.taskflow.notification.config;

import com.taskflow.notification.realtime.JwtHandshakeInterceptor;
import com.taskflow.notification.realtime.UserHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtInterceptor;
    private final UserHandshakeHandler userHandshakeHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /user/queue/notifications: private mỗi user (Spring chuyển /user/{id}/queue/...)
        // /topic/board/{boardId}: public broadcast
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .addInterceptors(jwtInterceptor)
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // Endpoint không SockJS cho client native WebSocket
        registry.addEndpoint("/ws/notifications")
                .addInterceptors(jwtInterceptor)
                .setHandshakeHandler(userHandshakeHandler)
                .setAllowedOriginPatterns("*");
    }
}
