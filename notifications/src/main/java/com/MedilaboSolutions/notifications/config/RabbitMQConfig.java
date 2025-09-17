package com.MedilaboSolutions.notifications.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue for notifications after doctor decision
    public static final String NOTIFICATION_QUEUE_NAME = "assessment-report-ready";

    @Bean
    public Queue assessmentReportQueue() {
        // durable=false because it's only for notifications
        return new Queue(NOTIFICATION_QUEUE_NAME, false);
    }

    // JSON message converter for RabbitMQ to serialize/deserialize messages as JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure RabbitTemplate with the JSON message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}