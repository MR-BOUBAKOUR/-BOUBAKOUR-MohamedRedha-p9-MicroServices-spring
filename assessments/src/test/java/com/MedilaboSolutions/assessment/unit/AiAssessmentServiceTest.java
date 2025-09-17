package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.controller.AssessmentSseController;
import com.MedilaboSolutions.assessment.dto.AiAssessmentResponse;
import com.MedilaboSolutions.assessment.model.Assessment;
import com.MedilaboSolutions.assessment.service.AiAssessmentService;
import com.MedilaboSolutions.assessment.service.AiSummarizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssessmentServiceTest {

    @Mock
    private ChatClient chatClient;
    @Mock
    private VectorStoreDocumentRetriever documentRetriever;
    @Mock
    private AiSummarizerService aiSummarizerService;
    @Mock
    private AssessmentSseController sseController;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private AiAssessmentService aiAssessmentService;

    @BeforeEach
    void setUp() {
        aiAssessmentService = new AiAssessmentService(
                chatClient, documentRetriever, aiSummarizerService, sseController
        );
    }

    @Test
    @DisplayName("Should return VERY_LOW when medical notes are blank")
    void shouldReturnVeryLowWhenMedicalNotesAreBlank() {
        // Given
        Assessment assessment = createTestAssessment();

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", "", assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("VERY_LOW");
        assertThat(response.context()).isEqualTo("Absence de notes.");
        assertThat(response.analysis()).isEqualTo("Impossible de conclure objectivement.");
        verifyNoInteractions(documentRetriever, aiSummarizerService, chatClient);
    }

    @Test
    @DisplayName("Should return VERY_LOW when medical notes are null")
    void shouldReturnVeryLowWhenMedicalNotesAreNull() {
        // Given
        Assessment assessment = createTestAssessment();

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", null, assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("VERY_LOW");
        assertThat(response.context()).isEqualTo("Absence de notes.");
    }

    @Test
    @DisplayName("Should perform diabetes assessment with valid medical notes")
    void shouldPerformDiabetesAssessmentWithValidNotes() {
        // Given
        Assessment assessment = createTestAssessment();
        String medicalNotes = "Patient presents diabetes symptoms";

        List<Document> retrievedChunks = List.of(
                new Document("Medical guideline content", Map.of("page", "123", "ref", "ref-456"))
        );
        Document summarizedDocument = new Document("Summarized guidelines", Map.of());
        String aiResponseText = """
            NIVEAU: MODERATE
            ###
            CONTEXTE: Patient shows risk factors
            ###
            ANALYSE: Based on symptoms and age
            ###
            RECOMMANDATIONS: Schedule follow-up
            ###
            SOURCES: [[ref-456], page 123]
            """;

        when(documentRetriever.retrieve(any(Query.class))).thenReturn(retrievedChunks);
        when(aiSummarizerService.summarizeChunks(retrievedChunks)).thenReturn(summarizedDocument);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(aiResponseText);

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", medicalNotes, assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("MODERATE");
        assertThat(response.context()).isEqualTo("Patient shows risk factors");
        assertThat(response.analysis()).isEqualTo("Based on symptoms and age");
        assertThat(response.recommendations()).isEqualTo("Schedule follow-up");
        assertThat(response.sources()).isEqualTo("[[ref-456], page 123]");

        verify(documentRetriever).retrieve(any(Query.class));
        verify(aiSummarizerService).summarizeChunks(retrievedChunks);
        verify(sseController, times(2)).emitAssessmentProgress(anyLong(), anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle empty retrieved chunks")
    void shouldHandleEmptyRetrievedChunks() {
        // Given
        Assessment assessment = createTestAssessment();
        String medicalNotes = "Patient notes";

        when(documentRetriever.retrieve(any(Query.class))).thenReturn(List.of());

        String aiResponseText = """
            NIVEAU: LOW
            ###
            CONTEXTE: Limited information
            ###
            ANALYSE: No specific guidelines found
            ###
            RECOMMANDATIONS: General monitoring
            ###
            SOURCES: 
            """;

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(aiResponseText);

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", medicalNotes, assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("LOW");
        verify(aiSummarizerService, never()).summarizeChunks(any());
    }

    @Test
    @DisplayName("Should handle AI service exception and return error response")
    void shouldHandleAiServiceException() {
        // Given
        Assessment assessment = createTestAssessment();
        String medicalNotes = "Patient notes";

        when(documentRetriever.retrieve(any(Query.class)))
                .thenThrow(new RuntimeException("AI service error"));

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", medicalNotes, assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("ERROR");
        assertThat(response.context()).isEqualTo("Impossible d'évaluer le risque");
        assertThat(response.analysis()).isEqualTo("Erreur technique lors de l'analyse");
        assertThat(response.recommendations()).isEqualTo("Veuillez réessayer plus tard");
    }

    @Test
    @DisplayName("Should parse malformed AI response gracefully")
    void shouldParseMalformedAiResponse() {
        // Given
        Assessment assessment = createTestAssessment();
        String medicalNotes = "Patient notes";

        when(documentRetriever.retrieve(any(Query.class))).thenReturn(List.of());

        String malformedResponse = "Invalid response format";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(malformedResponse);

        // When
        AiAssessmentResponse response = aiAssessmentService.assessDiabetesRisk(
                45, "M", medicalNotes, assessment
        );

        // Then
        assertThat(response.level()).isEqualTo("ERROR"); // Default value
        assertThat(response.context()).isEqualTo("Données manquantes"); // Default value
    }

    private Assessment createTestAssessment() {
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setPatId(1L);
        return assessment;
    }
}