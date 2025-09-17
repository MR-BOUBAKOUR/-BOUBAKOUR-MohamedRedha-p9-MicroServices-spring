package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.PdfService;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Mock
    private PatientFeignClient patientFeignClient;

    @Mock
    private AssessmentService assessmentService;

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService(patientFeignClient, assessmentService);
    }

    @Test
    @DisplayName("Should generate PDF for accepted assessment")
    void shouldGeneratePdfForAcceptedAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto assessment = createAcceptedAssessment();
        PatientDto patient = createTestPatient();

        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(assessment);
        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", patient)));

        // When
        byte[] pdfBytes = pdfService.generatePdfAssessment(assessmentId, correlationId);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000); // PDF should have reasonable size

        // Verify PDF header (PDF files always start with %PDF)
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Should generate PDF for updated assessment")
    void shouldGeneratePdfForUpdatedAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto assessment = createUpdatedAssessment();
        PatientDto patient = createTestPatient();

        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(assessment);
        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", patient)));

        // When
        byte[] pdfBytes = pdfService.generatePdfAssessment(assessmentId, correlationId);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should generate PDF for manual assessment")
    void shouldGeneratePdfForManualAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto assessment = createManualAssessment();
        PatientDto patient = createTestPatient();

        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(assessment);
        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", patient)));

        // When
        byte[] pdfBytes = pdfService.generatePdfAssessment(assessmentId, correlationId);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should throw exception for pending assessment")
    void shouldThrowExceptionForPendingAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto pendingAssessment = createPendingAssessment();
        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(pendingAssessment);

        // When/Then
        assertThatThrownBy(() -> pdfService.generatePdfAssessment(assessmentId, correlationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PDF generation not allowed for the assessment status: PENDING");
    }

    @Test
    @DisplayName("Should throw exception for queued assessment")
    void shouldThrowExceptionForQueuedAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto queuedAssessment = createQueuedAssessment();
        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(queuedAssessment);

        // When/Then
        assertThatThrownBy(() -> pdfService.generatePdfAssessment(assessmentId, correlationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PDF generation not allowed for the assessment status: QUEUED");
    }

    @Test
    @DisplayName("Should throw exception when patient data not found")
    void shouldThrowExceptionWhenPatientDataNotFound() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        AssessmentDto assessment = createAcceptedAssessment();
        when(assessmentService.findAssessmentById(assessmentId)).thenReturn(assessment);
        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", null)));

        // When/Then
        assertThatThrownBy(() -> pdfService.generatePdfAssessment(assessmentId, correlationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Patient data not found");
    }

    private AssessmentDto createAcceptedAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setLevel("HIGH");
        assessment.setStatus("ACCEPTED");
        assessment.setAnalysis("Le patient présente des facteurs de risque élevés");
        return assessment;
    }

    private AssessmentDto createUpdatedAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setLevel("MODERATE");
        assessment.setStatus("UPDATED");
        assessment.setAnalysis("L'analyse mise à jour indique un risque modéré");
        return assessment;
    }

    private AssessmentDto createManualAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setLevel("LOW");
        assessment.setStatus("MANUAL");
        assessment.setAnalysis("L'évaluation manuelle indique un faible risque");
        return assessment;
    }


    private AssessmentDto createPendingAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setStatus("PENDING");
        return assessment;
    }

    private AssessmentDto createQueuedAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setStatus("QUEUED");
        return assessment;
    }

    private AssessmentDto createComprehensiveAssessment() {
        AssessmentDto assessment = new AssessmentDto();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setLevel("HIGH");
        assessment.setStatus("ACCEPTED");
        assessment.setContext(List.of(
                "Le patient a des antécédents familiaux de diabète",
                "IMC supérieur à la normale",
                "Historique de glycémies élevées"
        ));
        assessment.setAnalysis("Analyse complète indiquant un risque élevé de diabète basé sur plusieurs facteurs, y compris les antécédents familiaux, l'IMC et les résultats de tests précédents");
        assessment.setRecommendations(List.of(
                "Programmer une consultation immédiate avec un endocrinologue",
                "Commencer des modifications alimentaires",
                "Mettre en place une surveillance régulière de la glycémie",
                "Augmenter l'activité physique"
        ));
        assessment.setSources(List.of(
                "Référence du guide médical A",
                "Référence de l'étude clinique B"
        ));
        return assessment;
    }

    private PatientDto createTestPatient() {
        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1975, 6, 15));
        patient.setGender("M");
        return patient;
    }
}