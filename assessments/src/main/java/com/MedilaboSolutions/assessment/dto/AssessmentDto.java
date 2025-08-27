package com.MedilaboSolutions.assessment.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

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

    private Instant createdAt;
    private Instant updatedAt;
}
