package com.taskflow.project.client;

import com.taskflow.common.dto.ApiResponse;
import com.taskflow.common.exception.BadRequestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "user-service", fallbackMethod = "verifyExistsFallback")
    public Map<Long, Boolean> verifyExists(List<Long> userIds) {
        ApiResponse<Map<Long, Boolean>> resp = webClientBuilder.build()
                .post()
                .uri("http://taskflow-user/internal/users/exists")
                .bodyValue(userIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<Long, Boolean>>>() {})
                .timeout(Duration.ofSeconds(2))
                .block();

        if (resp == null || resp.getData() == null) {
            return Map.of();
        }
        // Jackson deserialize Map keys as String, but our type says Long. Re-map.
        Map<String, Boolean> raw = objectMapper.convertValue(resp.getData(), new TypeReference<>() {});
        return raw.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                e -> Long.valueOf(e.getKey()), Map.Entry::getValue));
    }

    public Map<Long, Boolean> verifyExistsFallback(List<Long> userIds, Throwable ex) {
        log.warn("UserService unavailable when verifying users {}: {}", userIds, ex.getMessage());
        throw new BadRequestException("user_service_unavailable");
    }
}
