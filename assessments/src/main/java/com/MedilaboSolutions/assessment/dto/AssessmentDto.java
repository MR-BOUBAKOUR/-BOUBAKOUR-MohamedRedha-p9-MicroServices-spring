package com.MedilaboSolutions.assessment.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {

    private Long id;

    private Long patId;
    private String level;
    private String status;

    private List<String> context;
    private String analysis;
    private List<String> recommendations;
    private List<String> sources;

    private Instant createdAt;
    private Instant updatedAt;
}
