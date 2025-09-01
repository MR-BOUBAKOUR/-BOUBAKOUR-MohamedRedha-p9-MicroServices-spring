package com.MedilaboSolutions.assessment.repository;

import com.MedilaboSolutions.assessment.dto.AssessmentStatus;
import com.MedilaboSolutions.assessment.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByPatIdOrderByCreatedAtDesc(Long patId);

    List<Assessment> findByPatIdAndStatusInOrderByCreatedAtDesc(Long patId, List<AssessmentStatus> statuses);
}