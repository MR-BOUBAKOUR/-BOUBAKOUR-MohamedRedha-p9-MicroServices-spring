package com.MedilaboSolutions.assessment.controller;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/assessments")
@RestController
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/{patId}")
    public ResponseEntity<SuccessResponse<AssessmentDto>> getAssessmentByPatientId(
            @RequestHeader("medilabo-solutions-correlation-id") String correlationId,
            @PathVariable Long patId
    ) {
        log.info("Generating assessment for patientId={}", patId);
        AssessmentDto assessment = assessmentService.generateAssessment(patId, correlationId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Assessment fetched successfully", assessment));
    }

}
