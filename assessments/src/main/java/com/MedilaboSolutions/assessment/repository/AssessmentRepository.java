package com.MedilaboSolutions.assessment.repository;

import com.MedilaboSolutions.assessment.dto.AssessmentStatus;
import com.MedilaboSolutions.assessment.model.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    Page<Assessment> findByPatIdAndStatusIn(Long patId, List<AssessmentStatus> statuses, Pageable pageable);

}