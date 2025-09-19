package com.MedilaboSolutions.patient.service;

import com.MedilaboSolutions.patient.model.Patient;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.exception.ResourceNotFoundException;
import com.MedilaboSolutions.patient.mapper.PatientMapper;
import com.MedilaboSolutions.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public Page<PatientDto> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(patientMapper::toPatientDto);
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

    public void deleteById(Long id) {
        patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource not found."));

        patientRepository.deleteById(id);
    }

    public void updateEarlyOnsetMailSent(Long id, boolean mailSent) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource not found."));
        patient.setEarlyOnsetMailSent(mailSent);
        patientRepository.save(patient);
    }
}
