package com.taskflow.notification.realtime;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Tạo Principal từ userId đã lưu trong attributes (do JwtHandshakeInterceptor đặt vào).
 * Spring sẽ dùng Principal.getName() để định tuyến /user/{name}/queue/... → /queue/... của session.
 */
@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object uid = attributes.get(JwtHandshakeInterceptor.USER_ID_ATTR);
        if (uid == null) return null;
        String userId = uid.toString();
        return () -> userId;
    }
}
