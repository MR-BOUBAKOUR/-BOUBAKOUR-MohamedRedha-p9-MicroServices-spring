package com.MedilaboSolutions.assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "high-risk-assessments";

    @Bean
    public Queue highRiskAssessmentQueue() {
        return new Queue(QUEUE_NAME, false);
    }
}