package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.NoteDto;
import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssessmentService {

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;

    public AssessmentDto generateAssessment(Long patId) {

        ResponseEntity<SuccessResponse<PatientDto>> patient = patientFeignClient.getPatientById(patId);
        String gender = Objects.requireNonNull(patient.getBody()).getData().getGender().toLowerCase();
        int age = calculateAge(patient.getBody().getData().getBirthDate());
        log.info("Gender : " + gender);
        log.info("Age : " + age);

        ResponseEntity<SuccessResponse<List<NoteDto>>> notes = noteFeignClient.getNoteByPatientId(patId);
        int triggerCount = countMedicalTriggers(notes);


        String risk = evaluateRiskLevel(gender, age, triggerCount);

        return new AssessmentDto(patId, risk);
    }

    private int countMedicalTriggers(ResponseEntity<SuccessResponse<List<NoteDto>>> notes) {
        int count = 0;
        List<NoteDto> allNotes = Objects.requireNonNull(notes.getBody()).getData();

        List<String> triggers = List.of(
                "Hémoglobine A1C", "Microalbumine", "Taille", "Poids",
                "Fumeur", "Fumeuse", "Anormal", "Cholestérol",
                "Vertiges", "Rechute", "Réaction", "Anticorps"
        );

        for (NoteDto note : allNotes) {
            String noteContent = note.getNote().toLowerCase();
            log.info("Analyzing note: {}", noteContent);
            for (String trigger : triggers) {
                if (noteContent.contains(trigger.toLowerCase())) {
                    count++;
                    log.info("Found trigger: {} in note", trigger);
                }
            }
        }
        log.info("Total trigger count: {}", count);
        return count;
    }

    private String evaluateRiskLevel(String gender, int age, int triggerCount) {
        if (triggerCount == 0) {
            return "None";
        }
        if (age > 30) {
            if (triggerCount >= 8) return "Early onset";
            if (triggerCount >= 6) return "In Danger";
            if (triggerCount >= 2) return "Borderline";
        }
        else {
            if ("m".equals(gender)) {
                if (triggerCount >= 5) return "Early onset";
                if (triggerCount >= 3) return "In Danger";
            } else if ("f".equals(gender)) {
                if (triggerCount >= 7) return "Early onset";
                if (triggerCount >= 4) return "In Danger";
            }
        }

        return "None";
    }

    private int calculateAge(LocalDate birthday) {
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}
