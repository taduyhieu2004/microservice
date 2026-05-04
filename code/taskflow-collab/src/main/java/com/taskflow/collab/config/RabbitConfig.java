package com.taskflow.collab.config;

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

    // Activity log queue: subscribe nhiều routing key
    @Bean
    public Queue activityQueue() {
        return QueueBuilder.durable(Queues.COLLAB_ACTIVITY).build();
    }

    @Bean
    public Declarables activityBindings(Queue activityQueue, TopicExchange taskflowExchange) {
        var keys = List.of(
                RoutingKeys.PATTERN_TASK_ALL,
                RoutingKeys.PATTERN_PROJECT_ALL,
                RoutingKeys.PATTERN_BOARD_ALL,
                RoutingKeys.PATTERN_LIST_ALL,
                RoutingKeys.PATTERN_COMMENT_ALL,
                RoutingKeys.PATTERN_ATTACHMENT_ALL
        );
        var bindings = keys.stream()
                .map(k -> (Declarable) BindingBuilder.bind(activityQueue).to(taskflowExchange).with(k))
                .toList();
        return new Declarables(bindings);
    }

    // Cleanup queue: chỉ task.deleted, project.deleted để xoá comments + attachments
    @Bean
    public Queue cleanupQueue() {
        return QueueBuilder.durable(Queues.COLLAB_CLEANUP).build();
    }

    @Bean
    public Binding cleanupTaskDeleted(Queue cleanupQueue, TopicExchange taskflowExchange) {
        return BindingBuilder.bind(cleanupQueue).to(taskflowExchange).with(RoutingKeys.TASK_DELETED);
    }

    @Bean
    public Binding cleanupProjectDeleted(Queue cleanupQueue, TopicExchange taskflowExchange) {
        return BindingBuilder.bind(cleanupQueue).to(taskflowExchange).with(RoutingKeys.PROJECT_DELETED);
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
