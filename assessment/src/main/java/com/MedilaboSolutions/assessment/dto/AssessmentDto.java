package com.MedilaboSolutions.assessment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AssessmentDto {

    private Long patId;
    private String assessmentResult;

}
