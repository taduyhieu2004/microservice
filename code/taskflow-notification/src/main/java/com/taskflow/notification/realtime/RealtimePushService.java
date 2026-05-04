package com.taskflow.notification.realtime;

import com.taskflow.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimePushService {

    private final SimpMessagingTemplate messaging;

    public void pushToUser(Long userId, NotificationResponse notif) {
        // /user/{userId}/queue/notifications
        messaging.convertAndSendToUser(userId.toString(), "/queue/notifications", notif);
        log.debug("Pushed notification {} to userId={}", notif.getId(), userId);
    }

    public void broadcastBoardUpdate(Long boardId, String type, Map<String, Object> data) {
        // /topic/board/{boardId}
        Map<String, Object> payload = Map.of("type", type, "data", data);
        messaging.convertAndSend("/topic/board/" + boardId, payload);
        log.debug("Broadcast {} to board {}", type, boardId);
    }
}
