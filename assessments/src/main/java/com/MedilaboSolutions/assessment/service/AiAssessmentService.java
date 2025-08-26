package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AiAssessmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssessmentService {

    private final ChatClient chatClient;
    private final VectorStoreDocumentRetriever documentRetriever;
    private final SummarizerService summarizerService;

    public AiAssessmentResponse evaluateDiabetesRisk(int age, String gender, String medicalNotes) {

        if (medicalNotes == null || medicalNotes.isBlank()) {
            log.warn("Pas de notes médicales : retour direct VERY_LOW");
            return AiAssessmentResponse.builder()
                    .level("VERY_LOW")
                    .context("Absence de notes.")
                    .analysis("Impossible de conclure objectivement.")
                    .recommendations("Revoir le patient avec des données complémentaires.")
                    .build();
        }

        try {

            // Step 1: retrieve and summarize the relevant guideline chunks
            String relevantGuidelines = retrieveAndSummarizeChunks(medicalNotes);
            log.info("Résumé final des chunks pertinents : {}", relevantGuidelines);

            // Step 2: perform the diabetes risk assessment using patient data and summarized guidelines
            return performDiabetesAssessment(age, gender, medicalNotes, relevantGuidelines);

        } catch (Exception e) {
            log.error("Erreur lors de l'évaluation du risque diabétique", e);
            return AiAssessmentResponse.builder()
                    .level("ERROR")
                    .context("Impossible d'évaluer le risque")
                    .analysis("Erreur technique lors de l'analyse")
                    .recommendations("Veuillez réessayer plus tard")
                    .build();
        }
    }

    private String retrieveAndSummarizeChunks(String medicalNotes) {
        log.info("Recherche de chunks pertinents pour les notes médicales");

        // Build the query object from the medical notes for vector-based retrieval
        Query query = new Query(medicalNotes);
        List<Document> retrievedChunks = documentRetriever.retrieve(query);

        if (retrievedChunks.isEmpty()) {
            log.warn("Aucun chunk pertinent trouvé dans la base de connaissances");
            return "Aucune directive médicale spécifique trouvée.";
        }

        log.info("Trouvé {} chunks pertinents, résumé en cours...", retrievedChunks.size());

        for (int i = 0; i < retrievedChunks.size(); i++) {
            Document doc = retrievedChunks.get(i);
            log.info("=========================== Chunk #{} ===========================", i + 1);
            log.info("Chunk #{} : {}", i + 1, doc.getText());
            log.info("Metadata #{} : {}", i + 1, doc.getMetadata());
            log.info("=================================================================");
        }

        // Summarize retrieved chunks while keeping their references and page numbers
        Document summarizedChunks = summarizerService.summarizeChunks(retrievedChunks);

        return summarizedChunks.getText();
    }

    private AiAssessmentResponse performDiabetesAssessment(int age, String gender, String medicalNotes, String guidelines) {
        log.info("Évaluation du risque diabétique avec directives médicales");

        String systemPrompt = """
            Vous êtes une IA experte en évaluation du risque diabétique pour assistance médicale.
            Le destinataire étant un professionnel de santé, adopter un langage expert.
            
            Vous avez accès aux directives médicales suivantes, ainsi qu’à leurs sources :
            %s
            
            Basez votre évaluation sur :
            1. Les données du patient fournies
            2. Les directives médicales ci-dessus
            
            L'analyse doit citer explicitement les points pertinents des directives médicales et les mobiliser pour justifier le NIVEAU de risque.
            Évitez les généralités.
            
            Si directives médicales non-pertinentes ou absentes: votre expertise médicale
            Si les données sont insuffisantes: NIVEAU = "VERY_LOW" et CONTEXTE = "Données insuffisantes."

            Répondez strictement au format suivant, en 5 sections séparées par ### :
            
            NIVEAU: [VERY_LOW | LOW | MODERATE | HIGH]
            ###
            CONTEXTE: [Liste des faits observables et antécédents pertinents uniquement. Aucun diagnostic toléré dans cette section]
            ###
            ANALYSE: [Raisonnement médical justifiant le NIVEAU, 3-4 phrases, concis, sans répétitions]
            ###
            RECOMMANDATIONS: [Liste de 3 actions concrètes, spécifiques, orientées suivi ou traitement.]
            ###
            SOURCES: [Liste des paires refs/pages utilisées dans les sections ANALYSE et RECOMMANDATIONS
                Si aucune ref/page = format stricte: vide
                sinon = format stricte:
                - [[ref-A], page B]
                - [[ref-C], page D]
            ]
            """.formatted(guidelines);

        String userPrompt = """
            Évaluez le risque de diabète pour ce patient :
            Âge : %d
            Sexe : %s
            Notes médicales : %s
            """.formatted(age, gender, medicalNotes);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content()
                .trim();

        log.info("Évaluation du risque diabétique terminée");
        return parseResponse(response);
    }

    private AiAssessmentResponse parseResponse(String response) {
        String level = "ERROR";
        String context = "Données manquantes";
        String analysis = "Données manquantes";
        String recommendations = "Données manquantes";
        String sources = "Données manquantes";

        String[] sections = response.split("###");
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
            } else if (section.startsWith("SOURCES:")) {
                sources = section.substring("SOURCES:".length()).trim();
            }
        }

        return AiAssessmentResponse.builder()
                .level(level)
                .context(context)
                .analysis(analysis)
                .recommendations(recommendations)
                .sources(sources)
                .build();
    }
}