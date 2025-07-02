package com.MedilaboSolutions.patient.unit;

import com.MedilaboSolutions.patient.domain.Patient;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.exception.ResourceNotFoundException;
import com.MedilaboSolutions.patient.mapper.PatientMapper;
import com.MedilaboSolutions.patient.repository.PatientRepository;
import com.MedilaboSolutions.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDto patientDto;
    private PatientRequestDto patientRequestDto;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setGender("M");

        patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setFirstName("John");
        patientDto.setLastName("Doe");
        patientDto.setBirthDate(LocalDate.of(1990, 1, 1));
        patientDto.setGender("M");

        patientRequestDto = new PatientRequestDto();
        patientRequestDto.setFirstName("John");
        patientRequestDto.setLastName("Doe");
        patientRequestDto.setBirthDate(LocalDate.of(1990, 1, 1));
        patientRequestDto.setGender("M");
    }

    @Test
    @DisplayName("Should return list of patient DTOs when patients exist")
    void findAll_ShouldReturnListOfPatients() {
        // Given
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientMapper.toPatientDto(patient)).thenReturn(patientDto);

        // When
        List<PatientDto> result = patientService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(patientDto);
        verify(patientRepository).findAll();
        verify(patientMapper).toPatientDto(patient);
    }

    @Test
    @DisplayName("Should return patient DTO when ID exists")
    void findById_WhenPatientExists_ShouldReturnPatient() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toPatientDto(patient)).thenReturn(patientDto);

        // When
        PatientDto result = patientService.findById(1L);

        // Then
        assertThat(result).isEqualTo(patientDto);
        verify(patientRepository).findById(1L);
        verify(patientMapper).toPatientDto(patient);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient with ID does not exist")
    void findById_WhenPatientNotExists_ShouldThrowException() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ressource not found.");

        verify(patientRepository).findById(1L);
        verifyNoInteractions(patientMapper);
    }

    @Test
    @DisplayName("Should create and return patient DTO when input is valid")
    void create_ShouldCreateAndReturnPatient() {
        // Given
        when(patientMapper.toPatient(patientRequestDto)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toPatientDto(patient)).thenReturn(patientDto);

        // When
        PatientDto result = patientService.create(patientRequestDto);

        // Then
        assertThat(result).isEqualTo(patientDto);
        verify(patientMapper).toPatient(patientRequestDto);
        verify(patientRepository).save(patient);
        verify(patientMapper).toPatientDto(patient);
    }

    @Test
    @DisplayName("Should update and return patient DTO when ID exists")
    void update_WhenPatientExists_ShouldUpdateAndReturnPatient() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toPatient(patientRequestDto)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toPatientDto(patient)).thenReturn(patientDto);

        // When
        PatientDto result = patientService.update(1L, patientRequestDto);

        // Then
        assertThat(result).isEqualTo(patientDto);
        verify(patientRepository).findById(1L);
        verify(patientMapper).toPatient(patientRequestDto);
        verify(patientRepository).save(any(Patient.class));
        verify(patientMapper).toPatientDto(patient);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent patient")
    void update_WhenPatientNotExists_ShouldThrowException() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.update(1L, patientRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ressource not found.");

        verify(patientRepository).findById(1L);
        verifyNoMoreInteractions(patientRepository, patientMapper);
    }
}
