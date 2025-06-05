package com.MedilaboSolutions.assessment.controller;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.dto.SuccessResponse;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/assessment")
@RestController
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/{patId}")
    public ResponseEntity<SuccessResponse<AssessmentDto>> getAssessmentByPatientId(@PathVariable Long patId) {
        AssessmentDto assessment = assessmentService.generateAssessment(patId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new SuccessResponse<>(200, "Assessment fetched successfully", assessment));
    }

}
