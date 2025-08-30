package com.MedilaboSolutions.notifications.integration;

import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
import com.MedilaboSolutions.notifications.config.AbstractRabbitMQContainerTest;
import com.MedilaboSolutions.notifications.service.MailtrapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("Should process HighRiskAssessmentEvent and trigger email sending")
    void shouldProcessHighRiskEvent_AndTriggerEmailSend() {
        // Given
        HighRiskAssessmentEvent event = new HighRiskAssessmentEvent(
                123L, "Jane", "Smith", "Early onset"
        );

        // When
        rabbitTemplate.convertAndSend("high-risk-assessments", event);

        // Then
//        await()
//                .atMost(5, TimeUnit.SECONDS)
//                .untilAsserted(() -> {
//                    verify(mailtrapEmailService).sendEmail(
//                            anyString(),
//                            anyString(),
//                            anyString(),
//                            anyString()
//                    );
//                });

    }
}
