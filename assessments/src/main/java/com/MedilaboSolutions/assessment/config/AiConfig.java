package com.MedilaboSolutions.assessment.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.print.attribute.standard.Compression;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

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
    public Advisor customRagAdvisor(ChatClient.Builder chatClientBuilder, VectorStore pgVectorStore) {

        return RetrievalAugmentationAdvisor.builder()
//                // Add a query transformer to rewrite the input before retrieval (rewrite the medical record)
//                .queryTransformers(
//                        RewriteQueryTransformer.builder()
//                            .chatClientBuilder(chatClientBuilder)
//                            .promptTemplate(new PromptTemplate("""
//                                Vous êtes un assistant qui prépare les dossiers médicaux pour recherche dans un vector store.
//                                Reformulez et condensez les informations du dossier médical en français pour que le RAG retrouve les documents les plus pertinents.
//                                Ne changez pas le sens, restez concis.
//                            """))
//                            .build()
//                )
                // Configure the vector store retriever
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(pgVectorStore)
                        .similarityThreshold(0.75)
                        .topK(5)
                        .build())
                .documentPostProcessors((query, documents) -> {
                    System.out.println("Query: " + query + "\n");
                    documents.forEach(doc ->
                            System.out.println(
                                    "Retrieved text:\n" + doc.getText() + "\n" +
                                            "Metadata:\n" + doc.getMetadata() + "\n--------------------"
                            )
                    );
                    return documents;
                })
                .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, Advisor customRagAdvisor) {
        return chatClientBuilder
                .defaultAdvisors(customRagAdvisor)
                .build();
    }
}
