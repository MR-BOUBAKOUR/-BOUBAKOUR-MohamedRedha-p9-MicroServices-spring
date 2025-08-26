package com.MedilaboSolutions.assessment.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {

    private Long patId;

    private String level;
    private String context;
    private String analysis;
    private String recommendations;
    private String sources;

    private String status;
}
