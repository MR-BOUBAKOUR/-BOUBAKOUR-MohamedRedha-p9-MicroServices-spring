package com.MedilaboSolutions.notifications.service;

import com.MedilaboSolutions.notifications.Dto.HighRiskAssessmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendHighRiskEmail(HighRiskAssessmentEvent event) {
        log.info("[SIMULATED EMAIL] Sending HIGH RISK alert email...");
        log.info("To: medecin.medilabosolutions@gmail.com");
        log.info("Subject: High Risk Alert - Patient {}", event.getPatLastname());
        log.info("Body: Patient {} {} has been assessed as '{}'. Please take necessary action.",
                event.getPatFirstName(), event.getPatLastname(), event.getRiskLevel());
    }
}
