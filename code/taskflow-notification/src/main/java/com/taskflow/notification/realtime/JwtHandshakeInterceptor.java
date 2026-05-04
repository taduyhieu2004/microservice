package com.taskflow.notification.realtime;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Key;
import java.util.Map;

/**
 * Verify JWT từ query param `?token=...` hoặc Authorization header trong handshake.
 * Khi connect SockJS từ browser, header thường không gửi được nên dùng query param.
 * Lưu userId vào session attributes để UserHandshakeHandler trả về làm Principal.
 */
@Slf4j
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "userId";

    private final Key accessKey;

    public JwtHandshakeInterceptor(@Value("${jwt.access-token.secret-key}") String secret) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null) {
            log.debug("WS handshake rejected: no token");
            return false;
        }
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(accessKey).build()
                    .parseClaimsJws(token).getBody();
            String userId = claims.getSubject();
            attributes.put(USER_ID_ATTR, userId);
            log.debug("WS handshake OK userId={}", userId);
            return true;
        } catch (Exception e) {
            log.debug("WS handshake JWT invalid: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest s) {
            String q = s.getServletRequest().getParameter("token");
            if (q != null && !q.isBlank()) return q;
            String auth = s.getServletRequest().getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        }
        return null;
    }
}
