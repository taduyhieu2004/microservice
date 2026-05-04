package com.taskflow.notification.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String body;
    private String link;
    private Long readAt;
    private Map<String, Object> metadata;
    private Long createdAt;
}
