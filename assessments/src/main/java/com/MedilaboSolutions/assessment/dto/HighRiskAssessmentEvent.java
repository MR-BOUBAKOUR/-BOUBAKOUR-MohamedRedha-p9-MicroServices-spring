package com.MedilaboSolutions.assessment.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskAssessmentEvent {
    private Long patId;
    private String patFirstName;
    private String patLastname;
    private String riskLevel;
}
