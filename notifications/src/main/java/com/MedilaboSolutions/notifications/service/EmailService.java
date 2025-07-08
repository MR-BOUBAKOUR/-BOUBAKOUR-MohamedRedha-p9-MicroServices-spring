package com.MedilaboSolutions.notifications.service;

import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
import com.MedilaboSolutions.notifications.config.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final EmailProperties emailProperties;
    private final MailtrapEmailService mailtrapEmailService;

    public void sendHighRiskEmail(HighRiskAssessmentEvent event) {
        String subject = "High Risk Alert - Patient " + event.getPatLastname();
        String body = String.format("Patient %s %s has been assessed as '%s'. Please take necessary action.",
                event.getPatFirstName(),
                event.getPatLastname(),
                event.getRiskLevel()
        );

        mailtrapEmailService.sendEmail(
                emailProperties.getSender(),
                emailProperties.getRecipient(),
                subject,
                body
        );
    }
}
