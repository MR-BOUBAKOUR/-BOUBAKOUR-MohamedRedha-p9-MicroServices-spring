package com.MedilaboSolutions.assessment.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskAssessmentEvent implements Serializable {
    private Long patId;
    private String patFirstName;
    private String patLastname;
    private String riskLevel;
}
