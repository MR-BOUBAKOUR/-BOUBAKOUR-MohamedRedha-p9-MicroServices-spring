package com.MedilaboSolutions.assessment.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestAiConfig {

    @Bean
    @Primary
    public VectorStore mockVectorStore() {
        return mock(VectorStore.class);
    }

    @Bean
    @Primary
    public VectorStoreDocumentRetriever mockDocumentRetriever() {
        VectorStoreDocumentRetriever mock = mock(VectorStoreDocumentRetriever.class);

        // Default configuration: returns mocked documents
        List<Document> mockDocuments = List.of(
                new Document("Mock medical guideline content",
                        Map.of("pages", "123", "refs", "ref-456"))
        );

        when(mock.retrieve(any(Query.class))).thenReturn(mockDocuments);
        return mock;
    }

    @Bean
    @Primary
    public ChatClient mockChatClient() {
        ChatClient mockClient = mock(ChatClient.class);

        ChatClient.ChatClientRequestSpec mockRequestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec mockResponseSpec = mock(ChatClient.CallResponseSpec.class);

        // Mocking even the "granular" steps (system(), user(), call()) of the ChatClient
        // so that the full chain of method calls in production code can be executed in tests
        // without actually calling a real AI service. This ensures stability and reproducibility.
        when(mockClient.prompt()).thenReturn(mockRequestSpec);
        when(mockRequestSpec.system(any(String.class))).thenReturn(mockRequestSpec);
        when(mockRequestSpec.user(any(String.class))).thenReturn(mockRequestSpec);
        when(mockRequestSpec.call()).thenReturn(mockResponseSpec);

        // Default response for tests
        String defaultResponse = """
            NIVEAU: MODERATE
            ###
            CONTEXTE: Test context
            ###
            ANALYSE: Test analysis
            ###
            RECOMMANDATIONS: Test recommendations
            ###
            SOURCES: [[ref-456], page 123]
            """;

        when(mockResponseSpec.content()).thenReturn(defaultResponse);

        return mockClient;
    }
}