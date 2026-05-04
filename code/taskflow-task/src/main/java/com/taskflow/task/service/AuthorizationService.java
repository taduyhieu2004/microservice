package com.taskflow.task.service;

import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.task.client.ProjectServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    public static final List<String> ROLES = List.of("VIEWER", "COMMENTER", "EDITOR", "ADMIN", "OWNER");

    private static final String ROLE_PREFIX = "role:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ProjectServiceClient projectClient;
    private final StringRedisTemplate redis;

    public String getRole(Long projectId, Long userId) {
        String key = ROLE_PREFIX + projectId + ":" + userId;
        String cached = redis.opsForValue().get(key);
        if ("__NULL__".equals(cached)) return null;
        if (cached != null) return cached;

        String role = projectClient.getRole(projectId, userId);
        if (role == null) {
            redis.opsForValue().set(key, "__NULL__", TTL);
        } else {
            redis.opsForValue().set(key, role, TTL);
        }
        return role;
    }

    public void requireMember(Long projectId, Long userId) {
        if (getRole(projectId, userId) == null) {
            throw new ForbiddenException("not_a_member");
        }
    }

    public void requireRole(Long projectId, Long userId, String minRole) {
        String role = getRole(projectId, userId);
        if (role == null) throw new ForbiddenException("not_a_member");
        int lvl = ROLES.indexOf(role);
        int min = ROLES.indexOf(minRole);
        if (lvl < 0 || min < 0 || lvl < min) {
            throw new ForbiddenException("insufficient_role");
        }
    }

    public void evict(Long projectId, Long userId) {
        redis.delete(ROLE_PREFIX + projectId + ":" + userId);
    }
}
