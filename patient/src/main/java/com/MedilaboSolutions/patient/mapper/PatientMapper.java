package com.MedilaboSolutions.patient.mapper;

import com.MedilaboSolutions.patient.domain.Patient;
import com.MedilaboSolutions.patient.dto.PatientCreateDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientDto toClientDto(Patient patient);
    Patient toClient(PatientDto patientDto);
    Patient toClient(PatientCreateDto patientCreateDto);
}
