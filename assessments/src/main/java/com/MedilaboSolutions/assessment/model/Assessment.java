package com.MedilaboSolutions.assessment.model;

import com.MedilaboSolutions.assessment.dto.AssessmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patId;

    @Column
    private String level;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.NONE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_contexts", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "context_item", columnDefinition = "TEXT")
    private List<String> context;

    @Column(columnDefinition = "TEXT")
    private String analysis;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_recommendations", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "recommendation_item", columnDefinition = "TEXT")
    private List<String> recommendations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assessment_sources", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "source_item", columnDefinition = "TEXT")
    private List<String> sources;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
