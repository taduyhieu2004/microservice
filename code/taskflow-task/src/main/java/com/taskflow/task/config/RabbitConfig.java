package com.taskflow.task.config;

import com.taskflow.events.Queues;
import com.taskflow.events.RoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange taskflowExchange() {
        return new TopicExchange(RoutingKeys.EXCHANGE, true, false);
    }

    @Bean
    public Queue taskCleanupQueue() {
        return QueueBuilder.durable(Queues.TASK_CLEANUP).build();
    }

    @Bean
    public Binding bindProjectDeleted(Queue taskCleanupQueue, TopicExchange taskflowExchange) {
        return BindingBuilder.bind(taskCleanupQueue).to(taskflowExchange).with(RoutingKeys.PROJECT_DELETED);
    }

    @Bean
    public Binding bindBoardDeleted(Queue taskCleanupQueue, TopicExchange taskflowExchange) {
        return BindingBuilder.bind(taskCleanupQueue).to(taskflowExchange).with(RoutingKeys.BOARD_DELETED);
    }

    @Bean
    public Binding bindListDeleted(Queue taskCleanupQueue, TopicExchange taskflowExchange) {
        return BindingBuilder.bind(taskCleanupQueue).to(taskflowExchange).with(RoutingKeys.LIST_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(converter);
        t.setExchange(RoutingKeys.EXCHANGE);
        return t;
    }
}
