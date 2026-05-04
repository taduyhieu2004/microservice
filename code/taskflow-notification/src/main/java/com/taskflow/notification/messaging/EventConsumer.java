package com.taskflow.notification.messaging;

import com.taskflow.events.EventEnvelope;
import com.taskflow.events.Queues;
import com.taskflow.notification.realtime.RealtimePushService;
import com.taskflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final NotificationService notificationService;
    private final RealtimePushService realtime;

    @RabbitListener(queues = Queues.NOTIFICATION)
    public void onEvent(EventEnvelope<Map<String, Object>> envelope,
                        @Header(value = "amqp_receivedRoutingKey", required = false) String routingKey) {
        if (envelope == null || envelope.getData() == null) return;
        String type = envelope.getEventType() != null ? envelope.getEventType() : routingKey;
        Map<String, Object> data = envelope.getData();
        Long actorId = envelope.getActorId();

        switch (type) {
            case "task.created"      -> handleTaskCreated(data);
            case "task.assigned"     -> handleTaskAssigned(data, actorId);
            case "task.moved"        -> handleTaskMoved(data, actorId);
            case "task.due_soon"     -> handleTaskDueSoon(data);
            case "task.overdue"      -> handleTaskOverdue(data);
            case "task.deleted"      -> handleTaskDeleted(data, actorId);
            case "comment.added"     -> handleCommentAdded(data, actorId);
            case "project.member.added" -> handleMemberAdded(data, actorId);
            default -> log.debug("Notification: ignoring {}", type);
        }
    }

    private void handleTaskCreated(Map<String, Object> data) {
        Long boardId = num(data.get("board_id"));
        if (boardId != null) {
            realtime.broadcastBoardUpdate(boardId, "task.created", data);
        }
        // Notify assignee nếu có
        Long assigneeId = num(data.get("assignee_id"));
        Long taskId = num(data.get("task_id"));
        if (assigneeId != null && taskId != null) {
            notificationService.create(assigneeId, "TASK_ASSIGNED",
                    "You were assigned to a task",
                    "Task: " + data.get("title"),
                    "/projects/" + data.get("project_id") + "/boards/" + boardId + "/tasks/" + taskId,
                    data);
        }
    }

    private void handleTaskAssigned(Map<String, Object> data, Long actorId) {
        Long newAssignee = num(data.get("new_assignee_id"));
        Long taskId = num(data.get("task_id"));
        Long projectId = num(data.get("project_id"));
        if (newAssignee != null && !newAssignee.equals(actorId)) {
            notificationService.create(newAssignee, "TASK_ASSIGNED",
                    "You were assigned to a task",
                    "Task #" + taskId,
                    "/projects/" + projectId + "/tasks/" + taskId,
                    data);
        }
    }

    private void handleTaskMoved(Map<String, Object> data, Long actorId) {
        Long boardId = inferBoardFromData(data);
        if (boardId != null) {
            realtime.broadcastBoardUpdate(boardId, "task.moved", data);
        }
    }

    private void handleTaskDueSoon(Map<String, Object> data) {
        Long assigneeId = num(data.get("assignee_id"));
        Long taskId = num(data.get("task_id"));
        if (assigneeId != null && taskId != null) {
            notificationService.create(assigneeId, "TASK_DUE_SOON",
                    "Task due soon",
                    "Task #" + taskId + " is due in " + data.get("hours_remaining") + "h",
                    "/projects/" + data.get("project_id") + "/tasks/" + taskId,
                    data);
        }
    }

    private void handleTaskOverdue(Map<String, Object> data) {
        Long assigneeId = num(data.get("assignee_id"));
        Long taskId = num(data.get("task_id"));
        if (assigneeId != null && taskId != null) {
            notificationService.create(assigneeId, "TASK_OVERDUE",
                    "Task overdue",
                    "Task #" + taskId + " is overdue",
                    "/projects/" + data.get("project_id") + "/tasks/" + taskId,
                    data);
        }
    }

    private void handleTaskDeleted(Map<String, Object> data, Long actorId) {
        Long boardId = inferBoardFromData(data);
        if (boardId != null) {
            realtime.broadcastBoardUpdate(boardId, "task.deleted", data);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCommentAdded(Map<String, Object> data, Long actorId) {
        Long taskId = num(data.get("task_id"));
        Long projectId = num(data.get("project_id"));
        // Notify mentioned users
        Object mentioned = data.get("mentioned_user_ids");
        if (mentioned instanceof List<?> ids) {
            for (Object o : ids) {
                Long uid = num(o);
                if (uid != null && !uid.equals(actorId)) {
                    Map<String, Object> meta = new HashMap<>(data);
                    notificationService.create(uid, "COMMENT_MENTION",
                            "You were mentioned in a comment",
                            String.valueOf(data.getOrDefault("content_preview", "")),
                            "/projects/" + projectId + "/tasks/" + taskId,
                            meta);
                }
            }
        }
    }

    private void handleMemberAdded(Map<String, Object> data, Long actorId) {
        Long userId = num(data.get("user_id"));
        Long projectId = num(data.get("project_id"));
        if (userId != null && !userId.equals(actorId)) {
            notificationService.create(userId, "MEMBER_INVITED",
                    "You were invited to a project",
                    "Role: " + data.get("role"),
                    "/projects/" + projectId,
                    data);
        }
    }

    private Long inferBoardFromData(Map<String, Object> data) {
        Long bid = num(data.get("board_id"));
        if (bid == null) bid = num(data.get("from_board_id"));
        if (bid == null) bid = num(data.get("to_board_id"));
        return bid;
    }

    private static Long num(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.valueOf(o.toString()); } catch (NumberFormatException e) { return null; }
    }
}
