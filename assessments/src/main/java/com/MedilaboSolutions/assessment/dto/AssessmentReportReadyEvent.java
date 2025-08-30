package com.MedilaboSolutions.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentReportReadyEvent {
    private Long assessmentId;
    private Long patientId;

    private String correlationId;
}