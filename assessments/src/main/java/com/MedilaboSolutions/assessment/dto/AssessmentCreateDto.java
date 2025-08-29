package com.MedilaboSolutions.assessment.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssessmentCreateDto {

    private Long patId;
    private String level;
    private List<String> context;

    private String analysis;
    private List<String> recommendations;
    private List<String> sources;
}
