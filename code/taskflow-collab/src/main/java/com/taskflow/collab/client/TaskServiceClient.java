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
public class TaskServiceClient {

    private final WebClient.Builder webClientBuilder;

    public record TaskInfo(boolean exists, Long projectId, Long boardId, Long listId) {}

    @CircuitBreaker(name = "task-service", fallbackMethod = "verifyFallback")
    public TaskInfo verify(Long taskId) {
        ApiResponse<Map<String, Object>> resp = webClientBuilder.build()
                .get()
                .uri("http://taskflow-task/internal/tasks/{id}/exists", taskId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {})
                .timeout(Duration.ofSeconds(2))
                .block();
        if (resp == null || resp.getData() == null) {
            return new TaskInfo(false, null, null, null);
        }
        Map<String, Object> d = resp.getData();
        boolean exists = Boolean.TRUE.equals(d.get("exists"));
        if (!exists) return new TaskInfo(false, null, null, null);
        return new TaskInfo(true,
                num(d.get("project_id")), num(d.get("board_id")), num(d.get("list_id")));
    }

    public TaskInfo verifyFallback(Long taskId, Throwable ex) {
        log.warn("TaskService unavailable: {}", ex.getMessage());
        throw new BadRequestException("task_service_unavailable");
    }

    private static Long num(Object o) {
        return o == null ? null : Long.valueOf(o.toString());
    }
}
