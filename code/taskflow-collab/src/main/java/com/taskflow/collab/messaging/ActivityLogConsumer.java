package com.taskflow.collab.messaging;

import com.taskflow.collab.entity.ActivityLog;
import com.taskflow.collab.repository.ActivityLogRepository;
import com.taskflow.events.EventEnvelope;
import com.taskflow.events.Queues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLogConsumer {

    private final ActivityLogRepository repo;

    @RabbitListener(queues = Queues.COLLAB_ACTIVITY)
    @Transactional
    public void onEvent(EventEnvelope<Map<String, Object>> envelope,
                        @Header(value = "amqp_receivedRoutingKey", required = false) String routingKey) {
        if (envelope == null || envelope.getEventId() == null) {
            log.warn("Empty envelope, routingKey={}", routingKey);
            return;
        }

        // Idempotency
        if (repo.existsByEventId(envelope.getEventId())) {
            log.debug("Event {} already processed, skipping", envelope.getEventId());
            return;
        }

        String type = envelope.getEventType() != null ? envelope.getEventType() : routingKey;
        Map<String, Object> data = envelope.getData() != null ? envelope.getData() : new HashMap<>();

        ActivityLog a = new ActivityLog();
        a.setEventId(envelope.getEventId());
        a.setActorId(envelope.getActorId());
        a.setOccurredAt(envelope.getOccurredAt());
        a.setAction(type);
        a.setPayload(data);

        // Phân loại target
        String[] parts = type.split("\\.");
        String domain = parts.length > 0 ? parts[0].toUpperCase() : "UNKNOWN";
        a.setTargetType(switch (domain) {
            case "TASK" -> "TASK";
            case "PROJECT" -> "PROJECT";
            case "BOARD" -> "BOARD";
            case "LIST" -> "LIST";
            case "COMMENT" -> "COMMENT";
            case "ATTACHMENT" -> "ATTACHMENT";
            default -> domain;
        });

        // Extract id chính
        a.setTargetId(num(data.get(switch (a.getTargetType()) {
            case "TASK" -> "task_id";
            case "PROJECT" -> "project_id";
            case "BOARD" -> "board_id";
            case "LIST" -> "list_id";
            case "COMMENT" -> "comment_id";
            case "ATTACHMENT" -> "attachment_id";
            default -> "id";
        })));

        a.setProjectId(num(data.get("project_id")));

        repo.save(a);
        log.debug("Activity logged: {} target={}/{}", type, a.getTargetType(), a.getTargetId());
    }

    private static Long num(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.valueOf(o.toString()); } catch (NumberFormatException e) { return null; }
    }
}
