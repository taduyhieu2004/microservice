package com.taskflow.collab.messaging;

import com.taskflow.collab.entity.Attachment;
import com.taskflow.collab.repository.AttachmentRepository;
import com.taskflow.collab.repository.CommentRepository;
import com.taskflow.collab.service.MinioStorageService;
import com.taskflow.events.EventEnvelope;
import com.taskflow.events.Queues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupConsumer {

    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final MinioStorageService storage;

    @RabbitListener(queues = Queues.COLLAB_CLEANUP)
    @Transactional
    public void onEvent(EventEnvelope<Map<String, Object>> envelope,
                        @Header(value = "amqp_receivedRoutingKey", required = false) String routingKey) {
        if (envelope == null || envelope.getData() == null) return;
        String type = envelope.getEventType() != null ? envelope.getEventType() : routingKey;
        Map<String, Object> data = envelope.getData();

        switch (type) {
            case "task.deleted" -> handleTaskDeleted(num(data.get("task_id")));
            case "project.deleted" -> handleProjectDeleted(num(data.get("project_id")));
            default -> log.debug("Ignoring {}", type);
        }
    }

    private void handleTaskDeleted(Long taskId) {
        if (taskId == null) return;
        int comments = commentRepository.softDeleteByTask(taskId);

        // Hard cleanup: xóa file MinIO + soft delete attachment metadata
        List<Attachment> atts = attachmentRepository.findActiveByTask(taskId);
        for (Attachment a : atts) {
            storage.delete(a.getStorageKey());
            a.setDeleted(true);
            attachmentRepository.save(a);
        }
        log.info("Cleanup task={}: {} comments + {} attachments soft-deleted", taskId, comments, atts.size());
    }

    private void handleProjectDeleted(Long projectId) {
        if (projectId == null) return;
        int comments = commentRepository.softDeleteByProject(projectId);

        List<Attachment> atts = attachmentRepository.findActiveByProject(projectId);
        for (Attachment a : atts) {
            storage.delete(a.getStorageKey());
            a.setDeleted(true);
            attachmentRepository.save(a);
        }
        log.info("Cleanup project={}: {} comments + {} attachments soft-deleted", projectId, comments, atts.size());
    }

    private static Long num(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.valueOf(o.toString()); } catch (NumberFormatException e) { return null; }
    }
}
