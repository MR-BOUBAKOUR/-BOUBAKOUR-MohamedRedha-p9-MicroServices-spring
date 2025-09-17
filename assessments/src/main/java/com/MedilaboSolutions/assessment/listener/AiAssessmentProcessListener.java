package com.MedilaboSolutions.assessment.listener;

import com.MedilaboSolutions.assessment.config.RabbitMQConfig;
import com.MedilaboSolutions.assessment.dto.AiAssessmentProcessEvent;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AiAssessmentProcessListener {

    private final AssessmentService assessmentService;

    @RabbitListener(
            queues = RabbitMQConfig.AI_QUEUE_NAME,
            concurrency = "1"
    )
    public void handleQueuedAiAssessment(AiAssessmentProcessEvent event) {
        log.info("Received queued assessment for patientId={}, assessmentId={}, correlationId={}",
                event.patientId(), event.assessmentId(), event.correlationId());

        try {
            assessmentService.processQueuedAiAssessment(
                    event.assessmentId(),
                    event.patientId(),
                    event.correlationId()
            );

            log.info("Successfully processed assessmentId={} for patientId={}",
                    event.assessmentId(), event.patientId());

        } catch (Exception e) {
            log.error("Failed to process assessmentId={} for patientId={}, error={}",
                    event.assessmentId(), event.patientId(), e.getMessage(), e);
        }
    }
}
