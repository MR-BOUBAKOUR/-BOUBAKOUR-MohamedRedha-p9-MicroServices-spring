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

        String systemPrompt = """
            Vous êtes une IA experte en médecine qui évalue le risque de diabète.
            
            Répondez toujours strictement en JSON avec le format :
            {
              "level": "None | Borderline | In Danger | Early onset",
              "summary": "Résumé clair en 2-3 phrases",
              "recommendations": "Conseils spécifiques, actionnables, sans détails superflus"
            }
            
            Si les informations sont insuffisantes, définissez "level": "None" et mentionnez-le dans "summary".
            
            Ne rajoutez jamais de texte hors JSON.
            """;

        String userPrompt = """
            Évaluez le risque de diabète pour ce patient :
            Âge : %d
            Sexe : %s
            Notes médicales : %s
            """.formatted(age, gender, notesText);

        try {
            log.info("Appel à Ollama en cours...");
            AiAssessmentResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AiAssessmentResponse.class);
            log.info("Réponse IA reçue");

            return response;
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à Ollama", e);
            return AiAssessmentResponse.builder()
                    .level("Error")
                    .summary("Impossible d'évaluer le risque : erreur IA")
                    .recommendations("Veuillez réessayer plus tard")
                    .build();
        }
    }
}
