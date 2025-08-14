package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AiAssessmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiAssessmentService {

    private final ChatClient chatClient;

    public AiAssessmentService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AiAssessmentResponse evaluateDiabetesRisk(int age, String gender, String notesText) {

        if (notesText == null || notesText.isBlank()) {
            log.warn("Pas de notes médicales : retour direct VERY_LOW");
            return AiAssessmentResponse.builder()
                    .level("VERY_LOW")
                    .summary("Données insuffisantes pour évaluer le risque de diabète.")
                    .recommendations("Données insuffisantes.")
                    .build();
        }

        String systemPrompt = """

            - Vous êtes une IA experte en évaluation du risque diabétique pour assistance médicale.
            - Le destinataire étant un professionnel de santé, fournissez des évaluations cliniques précises et des recommandations spécialisées.
            - Si les données sont insuffisantes : NIVEAU = "VERY_LOW", SUMMARY doit le mentionner.

            Répondez strictement au format suivant, en 3 sections séparées par ### :
            
            NIVEAU: [Un seul de ces niveaux : VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH]
            ###
            SUMMARY: [Résumé clair en 2-3 phrases, concis et précis]
            ###
            RECOMMANDATIONS: [Conseils spécifiques, actionnables, sans détails superflus]

            """;

        String userPrompt = """
            Évaluez le risque de diabète pour ce patient :
            Âge : %d
            Sexe : %s
            Notes médicales : %s
            """.formatted(age, gender, notesText);

        try {
            log.info("Appel à Ollama en cours...");
            String  markdownResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content()
                    .trim();
            log.info("Réponse IA reçue et valide");
            return parseMarkdownResponse(markdownResponse);

        } catch (Exception e) {
            log.error("Erreur lors de l'appel ou du parsing IA", e);
            return AiAssessmentResponse.builder()
                    .level("ERROR")
                    .summary("Impossible d'évaluer le risque : erreur IA")
                    .recommendations("Veuillez réessayer plus tard")
                    .build();
        }
    }

    private AiAssessmentResponse parseMarkdownResponse(String markdown) {
        String level = "ERROR";
        String summary = "Données manquantes";
        String recommendations = "Données manquantes";

        String[] sections = markdown.split("###");
        for (String section : sections) {
            section = section.trim();
            if (section.startsWith("NIVEAU:")) {
                level = section.substring("NIVEAU:".length()).trim();
            } else if (section.startsWith("SUMMARY:")) {
                summary = section.substring("SUMMARY:".length()).trim();
            } else if (section.startsWith("RECOMMANDATIONS:")) {
                recommendations = section.substring("RECOMMANDATIONS:".length()).trim();
            }
        }

        return AiAssessmentResponse.builder()
                .level(level)
                .summary(summary)
                .recommendations(recommendations)
                .build();
    }
}
