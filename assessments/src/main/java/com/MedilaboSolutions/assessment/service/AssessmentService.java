package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssessmentService {

    public AssessmentService(PatientFeignClient patientFeignClient, NoteFeignClient noteFeignClient, ChatClient.Builder chatClientBuilder) {
        this.patientFeignClient = patientFeignClient;
        this.noteFeignClient = noteFeignClient;
        this.chatClient = chatClientBuilder.build();
    }

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final ChatClient chatClient;

    private static final String PROMPT = """
            Évaluez le risque de diabète pour ce patient selon ces données :
            
            Âge : {age} ans
            Sexe : {gender}
            Notes médicales : {notes}
            
            Si aucune note médicale pertinente n'est disponible, répondez "None" pour le niveau de risque, indiquez dans le résumé qu'il n'y a pas assez d'informations, et ne fournissez aucune recommandation.
            
            Répondez strictement au format suivant, en 3 sections séparées par ### :
            
            NIVEAU: [Un seul de ces niveaux : None, Borderline, In Danger, Early onset]
            ###
            RESUME: [Résumé clair en 2-3 phrases, concis et précis]
            ###
            RECOMMANDATIONS: [Conseils spécifiques, actionnables, sans détails superflus]
            """;

    public AssessmentDto generateAssessment(Long patId, String correlationId) {

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

        String gender = patient.getGender();
        int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
        String notesText = notes.stream()
                .map(NoteDto::getNote)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" / "));

        PromptTemplate promptTemplate = new PromptTemplate(PROMPT);
        Prompt prompt = promptTemplate.create(Map.of(
                "age", age,
                "gender", gender,
                "notes", notesText
        ));

        String aiResponse;
        try {
            log.info("Appel à Ollama avec prompt : {}", prompt.getContents());
            aiResponse = chatClient.prompt(prompt).call().content().trim();
            log.info("Réponse brute de l'IA: {}", aiResponse);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à Ollama", e);
            throw e;
        }

        String[] parts = aiResponse.split("###");

        String riskLevel = "None";
        String summary = "";
        String recommendations = "";

        if (parts.length >= 4) {
            riskLevel = cleanPart(parts[1]);
            summary = cleanPart(parts[2]);
            recommendations = cleanPart(parts[3]);
        } else {
            log.warn("Réponse IA inattendue, parsing incomplet : {}", aiResponse);
        }

        return new AssessmentDto(patId, riskLevel, summary, recommendations);
    }

    private String cleanPart(String text) {
        if (text == null) return "";
        String[] lines = text.trim().split("\n", 2);
        return (lines.length > 1) ? lines[1].trim() : lines[0].trim();
    }
}