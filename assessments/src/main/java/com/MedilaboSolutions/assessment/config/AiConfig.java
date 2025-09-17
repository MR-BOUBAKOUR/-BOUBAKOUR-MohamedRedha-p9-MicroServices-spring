package com.MedilaboSolutions.assessment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class AiConfig {

    @Value("${spring.ai.vectorstore.pgvector.table-name}")
    private String guideline_chunks;

    @Value("${spring.ai.vectorstore.pgvector.dimensions}")
    private int dimensions;

    @Value("${spring.ai.vectorstore.pgvector.initialize-schema}")
    private boolean initializeSchema;

    @Bean
    public VectorStore pgVectorStore(DataSource dataSource, EmbeddingModel embeddingModel) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName(guideline_chunks)
                .dimensions(dimensions)
                .initializeSchema(initializeSchema)
                .build();
    }

    @Bean
    public VectorStoreDocumentRetriever documentRetriever(VectorStore pgVectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(pgVectorStore)
                .similarityThreshold(0.75)
                .topK(5)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}