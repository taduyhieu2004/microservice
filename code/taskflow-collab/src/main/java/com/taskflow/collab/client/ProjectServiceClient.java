package com.taskflow.collab.client;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.exception.BadRequestException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectServiceClient {

    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "project-service", fallbackMethod = "getRoleFallback")
    public String getRole(Long projectId, Long userId) {
        ApiResponse<Map<String, Object>> resp = webClientBuilder.build()
                .get()
                .uri("http://taskflow-project/internal/projects/{p}/members/{u}/role", projectId, userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {})
                .timeout(Duration.ofSeconds(2))
                .block();
        if (resp == null || resp.getData() == null) return null;
        Object role = resp.getData().get("role");
        return role == null ? null : role.toString();
    }

    public String getRoleFallback(Long projectId, Long userId, Throwable ex) {
        log.warn("ProjectService unavailable: {}", ex.getMessage());
        throw new BadRequestException("project_service_unavailable");
    }
}
