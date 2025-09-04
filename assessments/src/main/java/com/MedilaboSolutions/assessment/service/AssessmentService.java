package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.config.RabbitMQConfig;
import com.MedilaboSolutions.assessment.controller.AssessmentSseController;
import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.mapper.AssessmentMapper;
import com.MedilaboSolutions.assessment.model.Assessment;
import com.MedilaboSolutions.assessment.repository.AssessmentRepository;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
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

    private final AssessmentSseController sseController;
    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> cachedRefsFromFile;

    public AssessmentService(
            PatientFeignClient patientFeignClient,
            NoteFeignClient noteFeignClient,
            AiAssessmentService aiAssessmentService,
            AssessmentRepository assessmentRepository,
            AssessmentMapper assessmentMapper,
            RabbitTemplate rabbitTemplate,
            AssessmentSseController sseController,
            @Value("classpath:docs/guidelines_result_refs.json") Resource refsFile,
            ObjectMapper objectMapper
    ) {
        this.patientFeignClient = patientFeignClient;
        this.noteFeignClient = noteFeignClient;
        this.rabbitTemplate = rabbitTemplate;
        this.aiAssessmentService = aiAssessmentService;
        this.assessmentRepository = assessmentRepository;
        this.assessmentMapper = assessmentMapper;
        this.sseController = sseController;

        // Load the JSON file into an independent variable
        try (InputStream is = refsFile.getInputStream()) {
            this.cachedRefsFromFile = objectMapper.readValue(is, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger le fichier de refs", e);
        }
    }

    public List<AssessmentDto> findAssessmentsByPatientId(Long patId) {
        List<AssessmentStatus> allowedStatuses = List.of(
                AssessmentStatus.QUEUED,
                AssessmentStatus.PROCESSING,
                AssessmentStatus.PENDING,
                AssessmentStatus.REFUSED_PENDING,
                AssessmentStatus.ACCEPTED,
                AssessmentStatus.UPDATED,
                AssessmentStatus.MANUAL
        );

        List<Assessment> assessments =
                assessmentRepository.findByPatIdAndStatusInOrderByCreatedAtDesc(patId, allowedStatuses);

        return assessments.stream()
                .map(assessmentMapper::toAssessmentDto)
                .toList();
    }

    public AssessmentDto findAssessmentById(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id " + assessmentId));

        return assessmentMapper.toAssessmentDto(assessment);
    }

    public AssessmentDto createAssessment(Long patId, AssessmentCreateDto newAssessment, String correlationId) {
        Assessment assessment = assessmentMapper.toAssessment(newAssessment);

        assessment.setPatId(patId);
        // Saving for the assessment id generation
        Assessment createdAssessment = assessmentRepository.save(assessment);

        // Using updateStatus to trigger the email event
        updateStatus(createdAssessment.getId(), AssessmentStatus.MANUAL, correlationId);

        return assessmentMapper.toAssessmentDto(createdAssessment);
    }

    public AssessmentDto updateAssessment(Long assessmentId, AssessmentDto updatedAssessment, String correlationId) {
        Assessment existingAssessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id " + assessmentId));

        existingAssessment.setLevel(updatedAssessment.getLevel());
        existingAssessment.setAnalysis(updatedAssessment.getAnalysis());
        existingAssessment.setContext(updatedAssessment.getContext());
        existingAssessment.setRecommendations(updatedAssessment.getRecommendations());
        existingAssessment.setSources(updatedAssessment.getSources());

        assessmentRepository.save(existingAssessment);

        // Using updateStatus to trigger the email event
        updateStatus(assessmentId, AssessmentStatus.UPDATED, correlationId);

        return assessmentMapper.toAssessmentDto(existingAssessment);
    }

    public AssessmentDto queueAiAssessmentForProcessing(Long patId, String correlationId) {

        Assessment assessment = new Assessment();

        assessment.setPatId(patId);
        // Saving for the assessment id generation
        Assessment createdAssessment = assessmentRepository.save(assessment);

        updateStatus(createdAssessment.getId(), AssessmentStatus.QUEUED, correlationId);

        AiAssessmentProcessEvent event = new AiAssessmentProcessEvent(
                createdAssessment.getId(),
                createdAssessment.getPatId(),
                correlationId
        );

        // Will be processed by AiAssessmentProcessListener
        rabbitTemplate.convertAndSend(RabbitMQConfig.AI_QUEUE_NAME, event);

        return assessmentMapper.toAssessmentDto(createdAssessment);
    }

    public void processQueuedAiAssessment(Long assessmentId, Long patId, String correlationId) {

        // Fetch the existing assessment
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id " + assessmentId));

        // Mark as processing
        updateStatus(assessment.getId(), AssessmentStatus.PROCESSING, correlationId);

        sseController.emitAssessmentProgress(assessmentId, assessment.getPatId(), "Récupération du dossier médical", 10);

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

        // AI Call
        AiAssessmentResponse aiResponse = aiAssessmentService.assessDiabetesRisk(age, gender, notesText, assessment);
        assert aiResponse != null;

        // Enrich the raw AI sources item by replacing the refs with their detailed content
        sseController.emitAssessmentProgress(assessment.getId(), assessment.getPatId(), "Vérification des références médicales", 90);
        String sourcesEnriched = enrichSources(aiResponse.sources());

        // Saving the assessment produced with the status "PENDING" so that the doctor can evaluate it
        sseController.emitAssessmentProgress(assessment.getId(), assessment.getPatId(), "Finalisation du rapport", 95);
        assessment.setLevel(aiResponse.level());
        assessment.setContext(parseBulletPoints(aiResponse.context()));
        assessment.setAnalysis(aiResponse.analysis());
        assessment.setRecommendations(parseBulletPoints(aiResponse.recommendations()));
        assessment.setSources(parseBulletPoints(sourcesEnriched));

        assessmentRepository.save(assessment);

        // Mark as pending -> ready for the decision-making
        updateStatus(assessment.getId(), AssessmentStatus.PENDING, correlationId);
    }

    public AssessmentDto updateStatus(Long assessmentId, AssessmentStatus newStatus, String correlationId) {
        Assessment existingAssessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found with id " + assessmentId));

        if (Set.of(AssessmentStatus.ACCEPTED, AssessmentStatus.UPDATED, AssessmentStatus.MANUAL, AssessmentStatus.REFUSED)
                .contains(existingAssessment.getStatus())) {
            throw new IllegalStateException(
                    "Assessment with status " + existingAssessment.getStatus() + " cannot be modified"
            );
        }

        existingAssessment.setUpdatedAt(Instant.now());
        existingAssessment.setStatus(newStatus);

        assessmentRepository.save(existingAssessment);
        log.info("Assessment {} updated to status {}", assessmentId, newStatus);

        AssessmentDto dto = assessmentMapper.toAssessmentDto(existingAssessment);

        if (newStatus == AssessmentStatus.QUEUED) {
            sseController.emitAssessmentProgress(assessmentId, dto.getPatId(), "En file d'attente", 0);
        }

        if (newStatus == AssessmentStatus.PROCESSING) {
            sseController.emitAssessmentProgress(assessmentId, dto.getPatId(), "Début du traitement", 5);
        }

        if (newStatus == AssessmentStatus.PENDING) {
            sseController.emitAssessmentGenerated(dto);
        }

        if (Set.of(AssessmentStatus.ACCEPTED, AssessmentStatus.UPDATED, AssessmentStatus.MANUAL).contains(newStatus)) {
            AssessmentReportReadyEvent event = new AssessmentReportReadyEvent(
                    existingAssessment.getId(),
                    existingAssessment.getPatId(),
                    correlationId
            );

            // Will be processed by NotificationListener in the notifications microservices
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE_NAME, event);
        }

        return dto;
    }

    private List<String> parseBulletPoints(String text) {
        if (text == null || text.isEmpty()) return List.of();
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("- ") ? s.substring(2).trim() : s)
                .toList();
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
                            "simulated_chunks.json (contient des informations médicales générées par ChatGPT: document supplémentaire ajouté pour illustrer la capacité de l'application à exploiter plusieurs fichiers dans notre RAG)."
                    );
                } else {
                    // Otherwise (ref with letters or no ref) → keep only the page
                    key = "page " + page;
                    content = "Rapport de prévention et dépistage du diabète de type 2, HAS.";
                }

                String enrichedLine = String.format("- [%s] : %s%n", key, content);

                enrichedSourceText.append(enrichedLine);
            }

            return patternMatched ? enrichedSourceText.toString().trim() : aiSourcesText;

        } catch (Exception e) {
            log.error("Erreur lors de l'enrichissement des sources", e);
            return aiSourcesText;
        }
    }
}