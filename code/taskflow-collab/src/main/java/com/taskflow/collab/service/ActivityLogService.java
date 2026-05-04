package com.taskflow.collab.service;

import com.taskflow.collab.client.TaskServiceClient;
import com.taskflow.collab.dto.response.ActivityLogResponse;
import com.taskflow.collab.entity.ActivityLog;
import com.taskflow.collab.repository.ActivityLogRepository;
import com.taskflow.common.dto.PageResponse;
import com.taskflow.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repo;
    private final AuthorizationService authz;
    private final TaskServiceClient taskClient;

    public PageResponse<ActivityLogResponse> listForProject(Long callerId, Long projectId, int page, int size) {
        authz.requireMember(projectId, callerId);
        Page<ActivityLog> p = repo.findByProjectIdOrderByOccurredAtDesc(projectId, PageRequest.of(page, size));
        return PageResponse.of(p.map(this::toResponse));
    }

    public PageResponse<ActivityLogResponse> listForTask(Long callerId, Long taskId, int page, int size) {
        TaskServiceClient.TaskInfo info = taskClient.verify(taskId);
        if (!info.exists()) throw NotFoundException.of("Task", taskId);
        authz.requireMember(info.projectId(), callerId);

        Page<ActivityLog> p = repo.findByTaskId(taskId, PageRequest.of(page, size));
        return PageResponse.of(p.map(this::toResponse));
    }

    private ActivityLogResponse toResponse(ActivityLog a) {
        return ActivityLogResponse.builder()
                .id(a.getId()).eventId(a.getEventId())
                .projectId(a.getProjectId()).targetType(a.getTargetType())
                .targetId(a.getTargetId()).action(a.getAction())
                .actorId(a.getActorId()).payload(a.getPayload())
                .occurredAt(a.getOccurredAt()).build();
    }
}
