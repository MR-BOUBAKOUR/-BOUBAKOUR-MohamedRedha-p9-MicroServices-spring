package com.MedilaboSolutions.assessment.dto;

public record AiAssessmentResponse(
        String level,
        String summary,
        String recommendations
) {}
