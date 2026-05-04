package com.taskflow.task.messaging;

import com.taskflow.events.EventEnvelope;
import com.taskflow.events.Queues;
import com.taskflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupConsumer {

    private final TaskRepository taskRepository;

    @RabbitListener(queues = Queues.TASK_CLEANUP)
    @Transactional
    public void onEvent(EventEnvelope<Map<String, Object>> envelope,
                        @Header(value = "amqp_receivedRoutingKey", required = false) String routingKey) {
        String type = envelope.getEventType() != null ? envelope.getEventType() : routingKey;
        Map<String, Object> data = envelope.getData();
        if (data == null) {
            log.warn("Empty event payload for {}", type);
            return;
        }

        switch (type) {
            case "project.deleted" -> {
                long projectId = ((Number) data.get("project_id")).longValue();
                int n = taskRepository.softDeleteByProject(projectId);
                log.info("Cascade delete: {} tasks soft-deleted for project {}", n, projectId);
            }
            case "board.deleted" -> {
                long boardId = ((Number) data.get("board_id")).longValue();
                int n = taskRepository.softDeleteByBoard(boardId);
                log.info("Cascade delete: {} tasks soft-deleted for board {}", n, boardId);
            }
            case "list.deleted" -> {
                long listId = ((Number) data.get("list_id")).longValue();
                int n = taskRepository.softDeleteByList(listId);
                log.info("Cascade delete: {} tasks soft-deleted for list {}", n, listId);
            }
            default -> log.debug("Ignoring event {}", type);
        }
    }
}
