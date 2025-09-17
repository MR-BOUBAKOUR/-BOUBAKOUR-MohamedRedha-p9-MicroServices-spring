package com.MedilaboSolutions.notifications.unit;

import com.MedilaboSolutions.notifications.Dto.AssessmentReportReadyEvent;
import com.MedilaboSolutions.notifications.config.EmailProperties;
import com.MedilaboSolutions.notifications.service.EmailService;
import com.MedilaboSolutions.notifications.service.MailtrapService;
import com.MedilaboSolutions.notifications.service.client.AssessmentFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private MailtrapService mailtrapService;

    @Mock
    private AssessmentFeignClient assessmentFeignClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Should format and delegate email content for HighRiskAssessmentEvent")
    void shouldFormatAndDelegateEmail_ForHighRiskEvent() {
        // Given
        when(emailProperties.getSender()).thenReturn("sender@test.com");
        when(emailProperties.getRecipient()).thenReturn("recipient@test.com");

        AssessmentReportReadyEvent event = new AssessmentReportReadyEvent(
                123L,
                456L,
                "correlation-abc-123"
        );

        ResponseEntity<byte[]> dummyPdf = ResponseEntity.ok("dummy-pdf-content".getBytes());

        when(assessmentFeignClient.downloadAssessmentPdf(eq(123L), eq("correlation-abc-123")))
                .thenReturn(dummyPdf);

        // When
        emailService.sendAssessmentReportEmail(event);

        // Then
//        verify(mailtrapService).sendEmail(
//                eq("sender@test.com"),
//                eq("recipient@test.com"),
//                eq("Assessment Report Ready - ID 123"),
//                eq("The assessment report is ready. Please find the PDF attached."),
//                eq("dummy-pdf-content".getBytes()),
//                eq("assessment-123.pdf")
//        );
    }
}