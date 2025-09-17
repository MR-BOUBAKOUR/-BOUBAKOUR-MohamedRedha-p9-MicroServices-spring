package com.MedilaboSolutions.assessment.mapper;

import com.MedilaboSolutions.assessment.dto.AssessmentCreateDto;
import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.model.Assessment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    AssessmentDto toAssessmentDto(Assessment entity);

    @Mapping(target = "id", ignore = true)
    Assessment toAssessment(AssessmentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assessment toAssessment(AssessmentCreateDto dto);

}