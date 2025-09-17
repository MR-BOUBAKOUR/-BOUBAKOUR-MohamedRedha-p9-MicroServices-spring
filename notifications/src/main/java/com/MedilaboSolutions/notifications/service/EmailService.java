package com.MedilaboSolutions.notifications.service;

import com.MedilaboSolutions.notifications.Dto.AssessmentReportReadyEvent;
import com.MedilaboSolutions.notifications.config.EmailProperties;
import com.MedilaboSolutions.notifications.service.client.AssessmentFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final EmailProperties emailProperties;
    private final AssessmentFeignClient assessmentFeignClient;

    private final MailtrapService mailtrapService;

    public void sendAssessmentReportEmail(AssessmentReportReadyEvent event) {

        ResponseEntity<byte[]> pdfResponse = assessmentFeignClient.downloadAssessmentPdf(
                event.getAssessmentId(),
                event.getCorrelationId()
        );

        byte[] pdfBytes = pdfResponse.getBody();
        String fileName = "assessment_" + event.getAssessmentId() + ".pdf";

        String subject = "Medilabo Solutions - Rapport d'Évaluation Disponible";
        String body = "Bonjour,\n\nVotre rapport d'évaluation médicale est prêt.\n\nMerci.";

        mailtrapService.sendEmail(
                emailProperties.getSender(),
                emailProperties.getRecipient(),
                subject,
                body,
                pdfBytes,
                fileName
        );

        log.info("📧 Email with PDF sent to {} - content : assessment {} of the patient {} - correlationId : {}",
                emailProperties.getRecipient(),
                event.getAssessmentId(),
                event.getPatientId(),
                event.getCorrelationId()
        );
    }
}

