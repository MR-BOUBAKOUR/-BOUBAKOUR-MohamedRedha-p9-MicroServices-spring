package com.MedilaboSolutions.patient.unit;

import com.MedilaboSolutions.patient.controller.PatientController;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.dto.PatientDto;
import com.MedilaboSolutions.patient.exception.ResourceNotFoundException;
import com.MedilaboSolutions.patient.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientDto patientDto;
    private PatientRequestDto patientRequestDto;

    @BeforeEach
    void setUp() {
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

//    @Test
//    @DisplayName("Should return patient list when patients exist")
//    void getAllPatients_ShouldReturnPatientsList() throws Exception {
//        // Given
//        when(patientService.findAll()).thenReturn(List.of(patientDto));
//
//        // When & Then
//        mockMvc.perform(get("/patients"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Patients fetched successfully"))
//                .andExpect(jsonPath("$.data").isArray())
//                .andExpect(jsonPath("$.data[0].id").value(1))
//                .andExpect(jsonPath("$.data[0].firstName").value("John"));
//
//        verify(patientService).findAll();
//    }

    @Test
    @DisplayName("Should return patient when ID exists")
    void getPatientById_WhenExists_ShouldReturnPatient() throws Exception {
        // Given
        when(patientService.findById(1L)).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(get("/patients/1")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Patient fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(patientService).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when patient with ID does not exist")
    void getPatientById_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(patientService.findById(1L)).thenThrow(new ResourceNotFoundException("Ressource not found."));

        // When & Then
        mockMvc.perform(get("/patients/1")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isNotFound());

        verify(patientService).findById(1L);
    }

    @Test
    @DisplayName("Should create patient when input data is valid")
    void createPatient_WithValidData_ShouldCreatePatient() throws Exception {
        // Given
        when(patientService.create(any(PatientRequestDto.class))).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Patient created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(patientService).create(any(PatientRequestDto.class));
    }

    @Test
    @DisplayName("Should update patient when input data is valid")
    void updatePatient_WithValidData_ShouldUpdatePatient() throws Exception {
        // Given
        when(patientService.update(eq(1L), any(PatientRequestDto.class))).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Patient updated successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(patientService).update(eq(1L), any(PatientRequestDto.class));
    }

    @Test
    @DisplayName("Should delete patient when ID exists")
    void deletePatient_WhenExists_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(patientService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/patients/1"))
                .andExpect(status().isNoContent());

        verify(patientService).deleteById(1L);
    }

    @Test
    @DisplayName("Should update earlyOnsetMailSent flag when input is valid")
    void updateEarlyOnsetMailSent_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(patientService).updateEarlyOnsetMailSent(1L, true);

        // When & Then
        mockMvc.perform(put("/patients/1/early-onset-mail")
                        .param("mailSent", "true")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isNoContent());

        verify(patientService).updateEarlyOnsetMailSent(1L, true);
    }
}
