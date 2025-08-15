package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.ChunkDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    @Value("classpath:/guidelines_docs/guidelines_result_chunks.json")
    private Resource guidelines;

    @EventListener(ApplicationReadyEvent.class)
    public void ingest() {
        log.info("Démarrage ingestion...");

        try {
            List<ChunkDto> chunks = objectMapper.readValue(
                    guidelines.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ChunkDto.class));
            log.info("JSON chargé, {} chunks trouvés", chunks.size());

            List<Document> guidelineDocuments = chunks.stream()
                    .map(chunk -> new Document(
                            chunk.getText(),
                            Map.of(
                                    "titles", chunk.getMetadata().getTitles(),
                                    "pages", chunk.getMetadata().getPages(),
                                    "refs", chunk.getMetadata().getRefs()
                            )))
                    .toList();
            log.info("Documents créés, {}", guidelineDocuments.size());

            vectorStore.accept(guidelineDocuments);
            log.info("guidelineDocuments chunks ingérés avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de l'ingestion", e);
        }
    }
}
