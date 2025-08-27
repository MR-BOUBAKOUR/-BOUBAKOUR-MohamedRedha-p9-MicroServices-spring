package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.mapper.AssessmentMapper;
import com.MedilaboSolutions.assessment.model.Assessment;
import com.MedilaboSolutions.assessment.repository.AssessmentRepository;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssessmentService {

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;

    private final AiAssessmentService aiAssessmentService;

    private final AssessmentRepository assessmentRepository;
    private final AssessmentMapper assessmentMapper;

    private final Map<String, String> cachedRefsFromFile;

    public AssessmentService(
            PatientFeignClient patientFeignClient,
            NoteFeignClient noteFeignClient,
            AiAssessmentService aiAssessmentService,
            AssessmentRepository assessmentRepository,
            AssessmentMapper assessmentMapper,
            @Value("classpath:docs/guidelines_result_refs.json") Resource refsFile,
            ObjectMapper objectMapper
    ) {
        this.patientFeignClient = patientFeignClient;
        this.noteFeignClient = noteFeignClient;
        this.aiAssessmentService = aiAssessmentService;
        this.assessmentRepository = assessmentRepository;
        this.assessmentMapper = assessmentMapper;

        // Load the JSON file into an independent variable
        try (InputStream is = refsFile.getInputStream()) {
            this.cachedRefsFromFile = objectMapper.readValue(is, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le fichier de refs", e);
        }
    }

    public List<AssessmentDto> findAssessmentsByPatientId(Long patId, String correlationId) {
        List<Assessment> assessments = assessmentRepository.findByPatIdOrderByCreatedAtDesc(patId);

        return assessments.stream()
                .map(assessmentMapper::toAssessmentDto)
                .toList();
    }

    public AssessmentDto generateAssessment(Long patId, String correlationId) {

        // Finding the patient data
        ResponseEntity<SuccessResponse<PatientDto>> patientResponse = patientFeignClient.getPatientById(patId, correlationId);
        if (patientResponse.getBody() == null || patientResponse.getBody().getData() == null) {
            throw new IllegalStateException("Patient data not found for ID " + patId);
        }
        PatientDto patient = patientResponse.getBody().getData();

        // Finding the notes of the patient
        ResponseEntity<SuccessResponse<List<NoteDto>>> notesResponse = noteFeignClient.getNoteByPatientId(patId, correlationId);
        if (notesResponse.getBody() == null || notesResponse.getBody().getData() == null) {
            throw new IllegalStateException("Notes not found for patient ID " + patId);
        }
        List<NoteDto> notes = notesResponse.getBody().getData();

        // Preparing the data for the AI
        int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
        String gender = patient.getGender();
        String notesText = notes.stream()
                .map(NoteDto::getNote)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" / "));

        // Using the AI for the assessment
        AiAssessmentResponse aiResponse = aiAssessmentService.evaluateDiabetesRisk(age, gender, notesText);
        assert aiResponse != null;

        // Enrich the raw AI sources by replacing refs with detailed content
        String sourcesEnriched = enrichSources(aiResponse.sources());

        // Saving the assessment produced with the status "PENDING" so that the doctor can evaluate it
        Assessment generatedAssessment = Assessment.builder()
                .patId(patId)
                .level(aiResponse.level())
                .context(aiResponse.context())
                .analysis(aiResponse.analysis())
                .recommendations(aiResponse.recommendations())
                .sources(sourcesEnriched)
                .status("PENDING")
                .build();

        assessmentRepository.save(generatedAssessment);

        // Returning the final result
        return assessmentMapper.toAssessmentDto(generatedAssessment);
    }

    private String enrichSources(String aiSourcesText) {
        try {
            // Pattern to handle: "- [[ref-XXX], page YYY]" AND "- [[], page YYY]" (without ref)
            Pattern pattern = Pattern.compile("-?\\s*\\[\\[(ref-[A-Za-z0-9]+)?\\],\\s*page\\s+(\\d+)\\]");

            Matcher matcher = pattern.matcher(aiSourcesText);

            StringBuilder enrichedSourceText = new StringBuilder();
            boolean patternMatched = false;

            while (matcher.find()) {
                patternMatched = true;

                String ref = matcher.group(1);   // e.g., "ref-328" or null if no ref
                String page = matcher.group(2);  // e.g., "328"

                String key;
                String content;

                if (ref != null && ref.matches("ref-\\d+")) {
                    // If ref exists AND contains only digits → keep only the ref
                    key = ref;
                    String jsonKey = "[" + ref + "]";

                    content = cachedRefsFromFile.getOrDefault(
                            jsonKey,
                            "Référence fictive (origine: depuis un document supplémentaire rajouté afin d'illustrer la capacité de l'application à exploiter plusieurs fichiers dans notre RAG)."
                    );
                } else {
                    // Otherwise (ref with letters or no ref) → keep only the page
                    key = "page " + page;
                    content = "Rapport de prévention et dépistage du diabète de type 2, HAS.";
                }

                String enrichedLine = String.format("- [%s] : %s", key, content);

                enrichedSourceText.append(enrichedLine);
            }

            return patternMatched ? enrichedSourceText.toString().trim() : aiSourcesText;

        } catch (Exception e) {
            log.error("Erreur lors de l'enrichissement des sources", e);
            return aiSourcesText;
        }
    }
}