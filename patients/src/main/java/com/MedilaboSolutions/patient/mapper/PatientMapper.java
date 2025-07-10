package com.MedilaboSolutions.patient.mapper;

import com.MedilaboSolutions.patient.model.Patient;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "earlyOnsetMailSent", source = "earlyOnsetMailSent")
    PatientDto toPatientDto(Patient patient);

    @Mapping(target = "earlyOnsetMailSent", ignore = true)
    Patient toPatient(PatientDto patientDto);

    @Mapping(target = "earlyOnsetMailSent", ignore = true)
    Patient toPatient(PatientRequestDto patientRequestDto);

}
