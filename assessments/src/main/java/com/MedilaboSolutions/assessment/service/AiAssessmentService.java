package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AiAssessmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssessmentService {

    private final ChatClient chatClient;

    public AiAssessmentResponse evaluateDiabetesRisk(int age, String gender, String notesText) {

        if (notesText == null || notesText.isBlank()) {
            log.warn("Pas de notes médicales : retour direct VERY_LOW");
            return AiAssessmentResponse.builder()
                    .level("VERY_LOW")
                    .context("Absence de notes.")
                    .analysis("Impossible de conclure objectivement.")
                    .recommendations("Revoir le patient avec des données complémentaires.")
                    .build();
        }

        String systemPrompt = """
            Vous êtes une IA experte en évaluation du risque diabétique pour assistance médicale.
            Le destinataire étant un professionnel de santé, adopter un langage expert.
            Vos réponses doivent se baser uniquement sur les données fournies. Ne pas inventer ou extrapoler d’informations absentes.
            Si les données fournies sont insuffisantes pour conclure objectivement : NIVEAU = "VERY_LOW" et CONTEXTE = "Données insuffisantes."

            Répondez strictement au format suivant, en 4 sections séparées par ### :
            
            NIVEAU: [VERY_LOW | LOW | MODERATE | HIGH | VERY_HIGH]
            ###
            CONTEXTE: [Liste des faits observables et antécédents pertinents]
            ###
            ANALYSE: [Raisonnement médical justifiant le NIVEAU, 3-4 phrases, concis, sans répétitions]
            ###
            RECOMMANDATIONS: [3 actions concrètes, spécifiques, orientées suivi ou traitement.]
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
                    .context("Impossible d'évaluer le risque")
                    .analysis("Erreur IA")
                    .recommendations("Veuillez réessayer plus tard")
                    .build();
        }
    }

    private AiAssessmentResponse parseMarkdownResponse(String markdown) {
        String level = "ERROR";
        String context = "Données manquantes";
        String analysis = "Données manquantes";
        String recommendations = "Données manquantes";

        String[] sections = markdown.split("###");
        for (String section : sections) {
            section = section.trim();
            if (section.startsWith("NIVEAU:")) {
                level = section.substring("NIVEAU:".length()).trim();
            } else if (section.startsWith("CONTEXTE:")) {
                context = section.substring("CONTEXTE:".length()).trim();
            } else if (section.startsWith("ANALYSE:")) {
                analysis = section.substring("ANALYSE:".length()).trim();
            } else if (section.startsWith("RECOMMANDATIONS:")) {
                recommendations = section.substring("RECOMMANDATIONS:".length()).trim();
            }
        }

        return AiAssessmentResponse.builder()
                .level(level)
                .context(context)
                .analysis(analysis)
                .recommendations(recommendations)
                .build();
    }
}
