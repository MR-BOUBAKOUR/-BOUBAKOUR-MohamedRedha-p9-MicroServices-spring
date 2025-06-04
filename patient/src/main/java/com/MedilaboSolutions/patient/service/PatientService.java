package com.MedilaboSolutions.patient.service;

import com.MedilaboSolutions.patient.domain.Patient;
import com.MedilaboSolutions.patient.dto.PatientCreateDto;
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
                .map(patientMapper::toClientDto)
                .toList();
    }

    public PatientDto findById(Long id) {
        return patientRepository.findById(id)
                .map(patientMapper::toClientDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource not found."));
    }

    public PatientDto create(PatientCreateDto clientDto) {
        Patient saved = patientRepository.save(patientMapper.toClient(clientDto));
        return patientMapper.toClientDto(saved);
    }
}
