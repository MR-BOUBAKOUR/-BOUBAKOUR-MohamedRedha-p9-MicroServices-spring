package com.MedilaboSolutions.notifications.listener;

import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
import com.MedilaboSolutions.notifications.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "high-risk-assessments")
    public void handleHighRiskEvent(HighRiskAssessmentEvent event) {
        log.info("Received high-risk event: {}", event);
        emailService.sendHighRiskEmail(event);
    }
}
