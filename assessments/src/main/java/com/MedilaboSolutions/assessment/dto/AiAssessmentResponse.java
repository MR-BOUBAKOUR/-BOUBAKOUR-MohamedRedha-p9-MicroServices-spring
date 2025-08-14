package com.MedilaboSolutions.assessment.dto;

import lombok.Builder;

@Builder
public record AiAssessmentResponse(
        String level,
        String summary,
        String recommendations
) {}
