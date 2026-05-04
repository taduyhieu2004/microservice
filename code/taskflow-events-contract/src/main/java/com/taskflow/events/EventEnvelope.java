package com.taskflow.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EventEnvelope<T> {

    private String eventId;
    private String eventType;
    private Integer schemaVersion;
    private Long occurredAt;
    private Long actorId;
    private String traceId;
    private T data;

    public static <T> EventEnvelope<T> of(String eventType, Long actorId, T data) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .schemaVersion(1)
                .occurredAt(Instant.now().toEpochMilli())
                .actorId(actorId)
                .data(data)
                .build();
    }
}
