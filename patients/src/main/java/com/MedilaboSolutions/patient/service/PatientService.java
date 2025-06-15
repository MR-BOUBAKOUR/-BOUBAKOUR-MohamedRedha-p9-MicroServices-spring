package com.MedilaboSolutions.patient.service;

import com.MedilaboSolutions.patient.domain.Patient;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.exception.ResourceNotFoundException;
import com.MedilaboSolutions.patient.mapper.PatientMapper;
import com.MedilaboSolutions.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toPatientDto)
                .toList();
    }

    public PatientDto findById(Long id) {
        return patientRepository.findById(id)
                .map(patientMapper::toPatientDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource not found."));
    }

    public PatientDto create(PatientRequestDto patientDto) {
        Patient saved = patientRepository.save(patientMapper.toPatient(patientDto));
        return patientMapper.toPatientDto(saved);
    }

    public PatientDto update(long id, PatientRequestDto patientDto) {
        patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource not found."));

        Patient patientToUpdate = patientMapper.toPatient(patientDto);
        patientToUpdate.setId(id);

        Patient saved = patientRepository.save(patientToUpdate);
        return patientMapper.toPatientDto(saved);
    }
}
