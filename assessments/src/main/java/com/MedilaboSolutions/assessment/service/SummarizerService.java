package com.MedilaboSolutions.assessment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummarizerService {

    private final ChatClient chatClient;

    public Document summarizeChunks(List<Document> retrievedChunks) {
        if (retrievedChunks == null || retrievedChunks.isEmpty()) {
            return new Document("Aucun contenu médical disponible.", Map.of());
        }

        log.info("Résumé de {} chunks en cours...", retrievedChunks.size());

        // Step 1: build the text including refs & pages for each chunk
        String combinedText = retrievedChunks.stream()
                .map(this::formatChunk)
                .collect(Collectors.joining("\n\n"));

        // Step 2: generate the summary using the AI model
        String summarizedText = generateSummary(combinedText);

        log.info("Résumé généré avec succès");
        return new Document(summarizedText, Map.of(
                "chunksCount", retrievedChunks.size(),
                "type", "summarized_guidelines"
        ));
    }

    private String formatChunk(Document doc) {
        String pages = String.valueOf(doc.getMetadata().getOrDefault("pages", "unknown"));
        String refs = String.valueOf(doc.getMetadata().getOrDefault("refs", "unknown"));

        return String.format("%s [refs=%s, pages=%s]", doc.getText(), refs, pages);
    }

    private String generateSummary(String combinedText) {
        try {
            return chatClient.prompt()
                    .system("""
                        Vous êtes un assistant médical expert spécialisé dans la synthèse des directives médicales. Votre objectif est de résumer les chunks que vous recevez.
        
                        INSTRUCTIONS :
                        - 1 chunk = 1 résumé en une phrase avec la paire refs/pages associées
                        - résume = critères, diagnostiques, seuils, et recommandations importantes
                        - Pas de répetitions

                        Répondez strictement au format suivant, en 2 sections séparées par ### :

                        RESUME:
                        - [Phrase résumé du chunk 1] [[ref-A], page B]
                        - [Phrase résumé du chunk 2] [[ref-C], page D]
                        ###
                        SOURCES: [Liste des paires refs/pages présentes dans la section RESUME - format strict: [[ref-A], page B]]
                    """)
                    .user(combinedText)
                    .call()
                    .content()
                    .trim();
        } catch (Exception e) {
            log.warn("Erreur lors du résumé IA, retour du texte original", e);
            return combinedText;
        }
    }
}