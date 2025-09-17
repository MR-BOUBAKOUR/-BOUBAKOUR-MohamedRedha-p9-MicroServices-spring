package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.service.AiSummarizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiSummarizerServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private AiSummarizerService aiSummarizerService;

    @BeforeEach
    void setUp() {
        aiSummarizerService = new AiSummarizerService(chatClient);
    }

    @Test
    @DisplayName("Should return empty document when no chunks provided")
    void shouldReturnEmptyDocumentWhenNoChunks() {
        // When
        Document result = aiSummarizerService.summarizeChunks(null);

        // Then
        assertThat(result.getText()).isEqualTo("Aucun contenu médical disponible.");
        assertThat(result.getMetadata()).isEmpty();
        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("Should return empty document when empty list provided")
    void shouldReturnEmptyDocumentWhenEmptyList() {
        // When
        Document result = aiSummarizerService.summarizeChunks(List.of());

        // Then
        assertThat(result.getText()).isEqualTo("Aucun contenu médical disponible.");
        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("Should summarize chunks successfully")
    void shouldSummarizeChunksSuccessfully() {
        // Given
        List<Document> chunks = List.of(
                new Document("Guidelines pour le dépistage du diabète chez l'adulte",
                        Map.of("pages", "10", "refs", "ref-123")),
                new Document("Critères d'évaluation des facteurs de risque",
                        Map.of("pages", "15", "refs", "ref-456"))
        );

        String expectedSummary = "RÉSUMÉ : Dépistage du diabète pour adultes [[ref-123], page 10]. Critères d'évaluation des risques [[ref-456], page 15].";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedSummary);

        // When
        Document result = aiSummarizerService.summarizeChunks(chunks);

        // Then
        assertThat(result.getText()).isEqualTo(expectedSummary);
        assertThat(result.getMetadata())
                .containsEntry("chunksCount", 2)
                .containsEntry("type", "summarized_guidelines");

        verify(chatClient).prompt();
        verify(requestSpec).system(anyString());
        verify(requestSpec).user(anyString());
    }

    @Test
    @DisplayName("Should handle chunks with missing metadata")
    void shouldHandleChunksWithMissingMetadata() {
        // Given
        List<Document> chunks = List.of(
                new Document("Contenu du guideline médical", Map.of()),
                new Document("Autre guideline", Map.of("pages", "20"))
        );

        String expectedSummary = "RÉSUMÉ : Résumé des guidelines médicales.";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(expectedSummary);

        // When
        Document result = aiSummarizerService.summarizeChunks(chunks);

        // Then
        assertThat(result.getText()).isEqualTo(expectedSummary);
        assertThat(result.getMetadata()).containsEntry("chunksCount", 2);
    }

    @Test
    @DisplayName("Should handle AI service exception and return original text")
    void shouldHandleAiServiceExceptionAndReturnOriginalText() {
        // Given
        List<Document> chunks = List.of(
                new Document("Contenu médical", Map.of("pages", "10", "refs", "ref-123"))
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("AI service error"));

        // When
        Document result = aiSummarizerService.summarizeChunks(chunks);

        // Then
        assertThat(result.getText()).contains("CHUNK 1:");
        assertThat(result.getText()).contains("Contenu médical");
        assertThat(result.getText()).contains("[refs=ref-123, pages=10]");
        assertThat(result.getMetadata()).containsEntry("chunksCount", 1);
    }

    @Test
    @DisplayName("Should format chunks correctly in combined text")
    void shouldFormatChunksCorrectlyInCombinedText() {
        // Given
        List<Document> chunks = List.of(
                new Document("Contenu premier chunk",
                        Map.of("pages", "10", "refs", "ref-001")),
                new Document("Contenu second chunk",
                        Map.of("pages", "20", "refs", "ref-002"))
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Résumé");

        // When
        aiSummarizerService.summarizeChunks(chunks);

        // Then - Verify the user prompt contains properly formatted chunks
        verify(requestSpec).user(argThat((String userPrompt) ->
                userPrompt.contains("CHUNK 1:") &&
                        userPrompt.contains("Contenu premier chunk [refs=ref-001, pages=10]") &&
                        userPrompt.contains("CHUNK 2:") &&
                        userPrompt.contains("Contenu second chunk [refs=ref-002, pages=20]")
        ));
    }
}