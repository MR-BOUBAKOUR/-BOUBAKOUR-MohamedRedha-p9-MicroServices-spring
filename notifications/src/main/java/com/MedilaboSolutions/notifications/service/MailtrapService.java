package com.MedilaboSolutions.notifications.service;

import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.EmailAttachment;
import io.mailtrap.model.request.emails.MailtrapMail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class MailtrapService {

    @Value("${mailtrap.token}")
    private String mailtrapToken;

    @Value("${mailtrap.inbox-id}")
    private Long mailtrapInboxId;

    public void sendEmail(String fromEmail,
                          String toEmail,
                          String subject,
                          String content,
                          byte[] attachmentBytes,
                          String attachmentFileName) {

        String encodedAttachment = Base64.getEncoder().encodeToString(attachmentBytes);

        MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(mailtrapInboxId)
                .token(mailtrapToken)
                .build();

        MailtrapMail mail = MailtrapMail.builder()
                .from(new Address(fromEmail, "MedilaboSolutions Notifications"))
                .to(List.of(new Address(toEmail)))
                .subject(subject)
                .text(content)
                .category("Assessment Report")
                .attachments(List.of(EmailAttachment.builder()
                        .filename(attachmentFileName)
                        .content(encodedAttachment)
                        .build())
                )
                .build();

        try {
            log.info("Sending email via Mailtrap...");
            // ⚠️ Enable for demo only
            // MailtrapClientFactory.createMailtrapClient(config).send(mail);
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }
}
