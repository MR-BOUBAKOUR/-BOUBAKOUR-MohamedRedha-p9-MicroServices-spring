package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssessmentService {

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final AiAssessmentService aiAssessmentService;

    public AssessmentService(PatientFeignClient patientFeignClient, NoteFeignClient noteFeignClient, AiAssessmentService aiAssessmentService, ChatClient.Builder builder) {
        this.patientFeignClient = patientFeignClient;
        this.noteFeignClient = noteFeignClient;
        this.aiAssessmentService = aiAssessmentService;
    }

    public AssessmentDto generateAssessment(Long patId, String correlationId) {

        // Finding the data
        ResponseEntity<SuccessResponse<PatientDto>> patientResponse = patientFeignClient.getPatientById(patId, correlationId);
        if (patientResponse.getBody() == null || patientResponse.getBody().getData() == null) {
            throw new IllegalStateException("Patient data not found for ID " + patId);
        }
        PatientDto patient = patientResponse.getBody().getData();

        ResponseEntity<SuccessResponse<List<NoteDto>>> notesResponse = noteFeignClient.getNoteByPatientId(patId, correlationId);
        if (notesResponse.getBody() == null || notesResponse.getBody().getData() == null) {
            throw new IllegalStateException("Notes not found for patient ID " + patId);
        }
        List<NoteDto> notes = notesResponse.getBody().getData();

        // Preparing the data
        int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
        String gender = patient.getGender();
        String notesText = notes.stream()
                .map(NoteDto::getNote)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" / "));

        // Using the AI for the assessment
        AiAssessmentResponse aiResponse = aiAssessmentService.evaluateDiabetesRisk(age, gender, notesText);

        // Returning the final result
        assert aiResponse != null;
        return new AssessmentDto(
                patId,
                aiResponse.level(),
                aiResponse.summary(),
                aiResponse.recommendations()
        );
    }
}