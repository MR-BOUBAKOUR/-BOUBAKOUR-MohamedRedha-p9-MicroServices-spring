package com.MedilaboSolutions.assessment.dto;

public record AiAssessmentProcessEvent(
        Long assessmentId,
        Long patientId,
        String correlationId
) {}