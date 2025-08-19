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

    @Value("classpath:/docs/guidelines_result_chunks.json")
    private Resource guidelines;

    @Value("classpath:/docs/simulated_chunks.json")
    private Resource simulatedChunks;

    @EventListener(ApplicationReadyEvent.class)
    public void ingest() {
        log.info("Démarrage ingestion...");

        ingestFile(guidelines, "guidelines_result_chunks.json");
        ingestFile(simulatedChunks, "simulated_chunks.json");
    }

    private void ingestFile(Resource resource, String fileName) {
        try {
            List<ChunkDto> chunks = objectMapper.readValue(
                    resource.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ChunkDto.class));
            log.info("{} chargé, {} chunks trouvés", fileName, chunks.size());

            List<Document> documents = chunks.stream()
                    .map(chunk -> new Document(
                            chunk.getText(),
                            Map.of(
                                    "titles", chunk.getMetadata().getTitles(),
                                    "pages", chunk.getMetadata().getPages(),
                                    "refs", chunk.getMetadata().getRefs()
                            )))
                    .toList();
            log.info("Documents créés depuis {}", fileName);

            vectorStore.accept(documents);
            log.info("Chunks de {} ingérés avec succès.", fileName);
        } catch (Exception e) {
            log.error("Erreur lors de l'ingestion de {}", fileName, e);
        }
    }
}
