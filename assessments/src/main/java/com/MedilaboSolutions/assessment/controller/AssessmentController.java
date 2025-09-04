package com.MedilaboSolutions.assessment.controller;

import com.MedilaboSolutions.assessment.dto.AssessmentCreateDto;
import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.AssessmentStatus;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/assessments")
@RestController
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final PdfService pdfService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<SuccessResponse<List<AssessmentDto>>> getAssessmentsByPatientId(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long patientId
    ) {
        log.info("Fetching assessments list for patientId={}", patientId);
        List<AssessmentDto> assessments = assessmentService.findAssessmentsByPatientId(patientId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Assessments fetched successfully", assessments));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<SuccessResponse<AssessmentDto>> getAssessmentById(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId
    ) {
        log.info("Fetching assessment with id={}", assessmentId);
        AssessmentDto assessment = assessmentService.findAssessmentById(assessmentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Assessment fetched successfully", assessment));
    }

    @PostMapping("/patient/{patientId}/queue")
    public ResponseEntity<SuccessResponse<AssessmentDto>> queueAiAssessmentForPatient(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long patientId
    ) {
        log.info("Queuing assessment generation request for patientId={}", patientId);

        AssessmentDto assessment = assessmentService.queueAiAssessmentForProcessing(patientId, correlationId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Assessment created and queued successfully", assessment));
    }

    @PostMapping("/patient/{patientId}/create")
    public ResponseEntity<SuccessResponse<AssessmentDto>> createAssessmentForPatient(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long patientId,
            @RequestBody AssessmentCreateDto newAssessment
    ) {
        log.info("Creating assessment for patientId={}", patientId);
        AssessmentDto savedAssessment = assessmentService.createAssessment(patientId, newAssessment, correlationId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(201, "Assessment created successfully", savedAssessment));
    }

    @PatchMapping("/{assessmentId}")
    public ResponseEntity<SuccessResponse<AssessmentDto>> updateAssessment(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId,
            @RequestBody AssessmentDto updatedAssessment
    ) {
        log.info("Updating assessment with id={}", assessmentId);
        AssessmentDto savedAssessment = assessmentService.updateAssessment(assessmentId, updatedAssessment, correlationId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Assessment updated successfully", savedAssessment));
    }

    @PatchMapping("/{assessmentId}/accept")
    public ResponseEntity<SuccessResponse<AssessmentDto>> acceptAssessment(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId
    ) {
        AssessmentDto updatedAssessment = assessmentService.updateStatus(assessmentId, AssessmentStatus.ACCEPTED, correlationId);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Assessment accepted", updatedAssessment));
    }

    @PatchMapping("/{assessmentId}/refuse-pending")
    public ResponseEntity<SuccessResponse<AssessmentDto>> refusePendingAssessment(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId
    ) {
        AssessmentDto updatedAssessment = assessmentService.updateStatus(assessmentId, AssessmentStatus.REFUSED_PENDING, correlationId);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Assessment pending refused", updatedAssessment));
    }

    @PatchMapping("/{assessmentId}/refuse")
    public ResponseEntity<SuccessResponse<AssessmentDto>> refuseAssessment(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId
    ) {
        AssessmentDto updatedAssessment = assessmentService.updateStatus(assessmentId, AssessmentStatus.REFUSED, correlationId);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Assessment refused", updatedAssessment));
    }

    @GetMapping("/{assessmentId}/download")
    public ResponseEntity<byte[]> downloadAssessmentPdf(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long assessmentId
    ) {
        log.info("Generating PDF for assessmentId={}", assessmentId);

        byte[] pdfBytes = pdfService.generatePdfAssessment(assessmentId, correlationId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=assessment_" + assessmentId + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
