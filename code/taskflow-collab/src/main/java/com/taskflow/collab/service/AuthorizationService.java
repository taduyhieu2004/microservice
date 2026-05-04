package com.taskflow.collab.service;

import com.taskflow.collab.client.ProjectServiceClient;
import com.taskflow.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    public static final List<String> ROLES = List.of("VIEWER", "COMMENTER", "EDITOR", "ADMIN", "OWNER");

    private static final String PREFIX = "role:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ProjectServiceClient client;
    private final StringRedisTemplate redis;

    public String getRole(Long projectId, Long userId) {
        String key = PREFIX + projectId + ":" + userId;
        String cached = redis.opsForValue().get(key);
        if ("__NULL__".equals(cached)) return null;
        if (cached != null) return cached;

        String role = client.getRole(projectId, userId);
        redis.opsForValue().set(key, role == null ? "__NULL__" : role, TTL);
        return role;
    }

    public void requireMember(Long projectId, Long userId) {
        if (getRole(projectId, userId) == null) throw new ForbiddenException("not_a_member");
    }

    public void requireRole(Long projectId, Long userId, String minRole) {
        String role = getRole(projectId, userId);
        if (role == null) throw new ForbiddenException("not_a_member");
        int lvl = ROLES.indexOf(role), min = ROLES.indexOf(minRole);
        if (lvl < 0 || min < 0 || lvl < min) throw new ForbiddenException("insufficient_role");
    }
}
