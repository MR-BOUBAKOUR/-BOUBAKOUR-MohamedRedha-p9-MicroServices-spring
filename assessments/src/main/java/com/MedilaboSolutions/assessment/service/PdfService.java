package com.MedilaboSolutions.assessment.service;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.PatientDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private final PatientFeignClient patientFeignClient;
    private final AssessmentService assessmentService;

    public byte[] generatePdfAssessment(Long assessmentId, String correlationId) {
        AssessmentDto assessment = assessmentService.findAssessmentById(assessmentId);

        if (!isPdfGenerationAllowed(assessment.getStatus())) {
            throw new IllegalStateException("PDF generation not allowed for the assessment status: " + assessment.getStatus());
        }

        ResponseEntity<SuccessResponse<PatientDto>> patientResponse =
                patientFeignClient.getPatientById(assessment.getPatId(), correlationId);

        if (patientResponse.getBody() == null || patientResponse.getBody().getData() == null) {
            throw new IllegalStateException("Patient data not found for ID " + assessment.getPatId());
        }

        PatientDto patient = patientResponse.getBody().getData();

        return createPdfDocument(assessment, patient);
    }

    private boolean isPdfGenerationAllowed(String status) {
        return switch (status) {
            case "ACCEPTED", "UPDATED", "MANUAL" -> true;
            default -> false;
        };
    }

    private byte[] createPdfDocument(AssessmentDto assessment, PatientDto patient) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Styles
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // Header
            Paragraph title = new Paragraph("Medilabo Solutions", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Rapport d'évaluation médicale", headerFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Patient data
            document.add(new Paragraph("Informations Patient", headerFont));
            PdfPTable patientTable = new PdfPTable(2);
            patientTable.setWidthPercentage(100);
            patientTable.setSpacingBefore(10);
            patientTable.setSpacingAfter(10);

            patientTable.addCell(new Phrase("Nom", normalFont));
            patientTable.addCell(new Phrase(patient.getLastName(), normalFont));

            patientTable.addCell(new Phrase("Prénom", normalFont));
            patientTable.addCell(new Phrase(patient.getFirstName(), normalFont));

            patientTable.addCell(new Phrase("Date de naissance", normalFont));
            patientTable.addCell(new Phrase(patient.getBirthDate().toString(), normalFont));

            document.add(patientTable);

            // Context
            if (assessment.getContext() != null && !assessment.getContext().isEmpty()) {
                Paragraph contextTitle = new Paragraph("Contexte", headerFont);
                contextTitle.setSpacingAfter(5); // <-- ici
                document.add(contextTitle);

                for (String item : assessment.getContext()) {
                    Paragraph listItem = new Paragraph("- " + item, normalFont);
                    listItem.setIndentationLeft(20);
                    document.add(listItem);
                }
                document.add(Chunk.NEWLINE);
            }

            // Analysis
            if (assessment.getAnalysis() != null && !assessment.getAnalysis().isBlank()) {
                Paragraph analysisTitle = new Paragraph("Analyse", headerFont);
                analysisTitle.setSpacingAfter(5);
                document.add(analysisTitle);

                Paragraph analysisContent = new Paragraph(assessment.getAnalysis(), normalFont);
                document.add(analysisContent);
                document.add(Chunk.NEWLINE);
            }

            // Risk level
            Paragraph riskTitle = new Paragraph("Niveau de risque :", headerFont);
            riskTitle.setSpacingAfter(5);
            document.add(riskTitle);
            Paragraph riskLevel = new Paragraph(assessment.getLevel(), normalFont);
            document.add(riskLevel);
            document.add(Chunk.NEWLINE);

            // Recommendations
            if (assessment.getRecommendations() != null && !assessment.getRecommendations().isEmpty()) {
                Paragraph recTitle = new Paragraph("Recommandations", headerFont);
                recTitle.setSpacingAfter(5);
                document.add(recTitle);

                for (String item : assessment.getRecommendations()) {
                    Paragraph listItem = new Paragraph("- " + item, normalFont);
                    listItem.setIndentationLeft(20);
                    document.add(listItem);
                }
                document.add(Chunk.NEWLINE);
            }

            // Footer
            String generationDate = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(java.time.ZoneId.of("Europe/Paris"))
                    .format(Instant.now());
            Paragraph footer = new Paragraph("Document généré le " + generationDate, footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();
            log.info("PDF generated successfully for assessment {}", assessment.getId());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for assessment {}", assessment.getId(), e);
            throw new IllegalStateException("PDF generation failed", e);
        }
    }
}
