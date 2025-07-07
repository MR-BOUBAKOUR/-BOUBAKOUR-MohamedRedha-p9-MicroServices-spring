package com.MedilaboSolutions.notifications.service;

import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${mailersend.api.token}")
    private String mailerSendToken;

    @Value("${notifications.doctor.email}")
    private String doctorEmail;

    public void sendHighRiskEmail(HighRiskAssessmentEvent event) {
        Email email = new Email();

        email.setFrom("Medilabo", "no-reply@medilabo.com");
        email.addRecipient("Médecin", doctorEmail);

        email.setSubject("ALERTE : Risque élevé détecté");

        String content = String.format("Le patient %s %s (ID: %d) a été évalué comme présentant un risque élevé de diabète : %s.",
                event.getPatFirstName(),
                event.getPatLastname(),
                event.getPatId(),
                event.getRiskLevel());

        email.setPlain(content);
        email.setHtml("<p><strong>Alerte risque élevé :</strong></p><p>" + content + "</p>");

        MailerSend mailerSend = new MailerSend();
        mailerSend.setToken(mailerSendToken);

        try {
            MailerSendResponse response = mailerSend.emails().send(email);
            log.info("Email envoyé avec succès. Message ID : {}", response.messageId);
        } catch (MailerSendException e) {
            log.error("Erreur lors de l'envoi de l'email : {}", e.getMessage(), e);
        }
    }
}
