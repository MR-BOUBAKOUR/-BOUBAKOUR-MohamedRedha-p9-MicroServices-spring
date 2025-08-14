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

        String promptText = """
            Évaluez le risque de diabète pour ce patient selon ces données :
            
            Âge : %d ans
            Sexe : %s
            Notes médicales : %s
            
            Si aucune note médicale pertinente n'est disponible, répondez "None" pour le niveau de risque, indiquez dans le résumé qu'il n'y a pas assez d'informations.
            
            Répondez strictement en JSON avec ce format :
        
            {
              "level": "None | Borderline | In Danger | Early onset",
              "summary": "Résumé clair en 2-3 phrases",
              "recommendations": "Conseils spécifiques, actionnables, sans détails superflus"
            }
            """.formatted(age, gender, notesText);

        try {
            log.info("Appel à Ollama avec prompt : {}", promptText);
            return chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(AiAssessmentResponse.class);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à Ollama", e);
            throw new RuntimeException("Erreur IA", e);
        }
    }
}
