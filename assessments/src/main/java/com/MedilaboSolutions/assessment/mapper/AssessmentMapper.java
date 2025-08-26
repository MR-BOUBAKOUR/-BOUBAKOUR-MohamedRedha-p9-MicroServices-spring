package com.MedilaboSolutions.assessment.mapper;

import com.MedilaboSolutions.assessment.dto.AssessmentDto;
import com.MedilaboSolutions.assessment.model.Assessment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    AssessmentDto toAssessmentDto(Assessment entity);

    @Mapping(target = "id", ignore = true)
    Assessment toAssessment(AssessmentDto dto);

}