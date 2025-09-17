package com.MedilaboSolutions.notifications.listener;

import com.MedilaboSolutions.notifications.Dto.AssessmentReportReadyEvent;
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

    @RabbitListener(queues = "assessment-report-ready")
    public void handleAssessmentReportEvent(AssessmentReportReadyEvent event) {
        log.info("Received assessment report event: {}", event);
        emailService.sendAssessmentReportEmail(event);
    }
}
