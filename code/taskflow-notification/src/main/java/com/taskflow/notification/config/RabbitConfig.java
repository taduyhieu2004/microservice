package com.taskflow.notification.config;

import com.taskflow.events.Queues;
import com.taskflow.events.RoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange taskflowExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(Queues.NOTIFICATION).build();
    }

    @Bean
    public Declarables notificationBindings(Queue notificationQueue, TopicExchange taskflowExchange) {
        var keys = List.of(
                RoutingKeys.PATTERN_TASK_ALL,
                RoutingKeys.PATTERN_PROJECT_MEMBER,
                RoutingKeys.PATTERN_COMMENT_ALL
        );
        var bindings = keys.stream()
                .map(k -> (Declarable) BindingBuilder.bind(notificationQueue).to(taskflowExchange).with(k))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter c) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(c);
        t.setExchange(RoutingKeys.EXCHANGE);
        return t;
    }
}
