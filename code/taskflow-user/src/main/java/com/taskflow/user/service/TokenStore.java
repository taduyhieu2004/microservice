package com.taskflow.user.service;

import com.taskflow.user.constant.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenStore {

    private static final String PREFIX = "jwt:";

    private final StringRedisTemplate redis;

    public void save(Long userId, TokenType type, String token, long ttlMillis) {
        redis.opsForValue().set(key(userId, type), token, Duration.ofMillis(ttlMillis));
    }

    public boolean isValid(Long userId, TokenType type, String token) {
        String stored = redis.opsForValue().get(key(userId, type));
        return token.equals(stored);
    }

    public void revoke(Long userId, TokenType type) {
        redis.delete(key(userId, type));
    }

    public void revokeAll(Long userId) {
        for (TokenType type : TokenType.values()) {
            revoke(userId, type);
        }
    }

    private String key(Long userId, TokenType type) {
        return PREFIX + userId + ":" + type.name();
    }
}
