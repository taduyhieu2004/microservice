package com.taskflow.project.messaging;

import com.taskflow.events.EventEnvelope;
import com.taskflow.events.RoutingKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public <T> void publish(String routingKey, Long actorId, T data) {
        EventEnvelope<T> envelope = EventEnvelope.of(routingKey, actorId, data);
        try {
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.debug("Published {} eventId={}", routingKey, envelope.getEventId());
        } catch (AmqpException e) {
            log.error("Failed to publish {}: {}", routingKey, e.getMessage());
        }
    }
}
