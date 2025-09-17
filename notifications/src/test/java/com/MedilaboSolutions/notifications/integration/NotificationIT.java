package com.MedilaboSolutions.notifications.integration;

import com.MedilaboSolutions.notifications.Dto.AssessmentReportReadyEvent;
import com.MedilaboSolutions.notifications.config.AbstractRabbitMQContainerTest;
import com.MedilaboSolutions.notifications.service.MailtrapService;
import com.MedilaboSolutions.notifications.service.client.AssessmentFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
public class NotificationIT extends AbstractRabbitMQContainerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private MailtrapService mailtrapService;

    @MockitoBean
    private AssessmentFeignClient assessmentFeignClient;

    @Test
    @DisplayName("Should process HighRiskAssessmentEvent and trigger email sending")
    void shouldProcessHighRiskEvent_AndTriggerEmailSend() {
        // Given
        AssessmentReportReadyEvent event = new AssessmentReportReadyEvent(
                123L,
                456L,
                "correlation-abc-123"
        );

        when(assessmentFeignClient.downloadAssessmentPdf(123L, "correlation-abc-123"))
                .thenReturn(ResponseEntity.ok("dummy-pdf-content".getBytes()));

        // When
        rabbitTemplate.convertAndSend("assessment-report-ready", event);

        // Then
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(mailtrapService).sendEmail(
                            anyString(),
                            anyString(),
                            anyString(),
                            anyString(),
                            any(byte[].class),
                            anyString()
                    );
                });

    }
}
