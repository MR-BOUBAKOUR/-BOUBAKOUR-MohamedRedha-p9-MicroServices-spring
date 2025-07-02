package com.MedilaboSolutions.patient.integration;

import com.MedilaboSolutions.patient.config.AbstractMySQLContainerTest;
import com.MedilaboSolutions.patient.dto.PatientRequestDto;
import com.MedilaboSolutions.patient.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
@Transactional
class PatientIT extends AbstractMySQLContainerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return existing patient by ID")
    void getPatientById_ShouldReturnExistingPatient() throws Exception {
        mockMvc.perform(get("/patients/1")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("TestNone"))
                .andExpect(jsonPath("$.data.lastName").value("Test"));
    }

    @Test
    @DisplayName("Should return list of all patients")
    void getAllPatients_ShouldReturnPatientsList() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(Matchers.greaterThanOrEqualTo(4))); // 4 patients dans le dump
    }

    @Test
    @DisplayName("Should update and return patient when ID exists")
    void updatePatient_WhenExists_ShouldUpdateAndReturnPatient() throws Exception {
        PatientRequestDto updateRequest = new PatientRequestDto();
        updateRequest.setFirstName("UpdatedFirstName");
        updateRequest.setLastName("UpdatedLastName");
        updateRequest.setBirthDate(LocalDate.of(1980, 1, 1));
        updateRequest.setGender("F");
        updateRequest.setAddress("UpdatedAddress");
        updateRequest.setPhone("999-999-9999");

        mockMvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.data.lastName").value("UpdatedLastName"))
                .andExpect(jsonPath("$.data.address").value("UpdatedAddress"));
    }

    @Test
    @DisplayName("Should create patient only if input data is valid")
    void createPatient_WithValidData_ShouldCreatePatient() throws Exception {
        PatientRequestDto createRequest = new PatientRequestDto();
        createRequest.setFirstName("New");
        createRequest.setLastName("Patient");
        createRequest.setBirthDate(LocalDate.of(1995, 5, 5));
        createRequest.setGender("M");
        createRequest.setAddress("New Address");
        createRequest.setPhone("123-123-1234");

        String createResponse = mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstName").value("New"))
                .andReturn().getResponse().getContentAsString();

        Long newPatientId = objectMapper.readTree(createResponse).get("data").get("id").asLong();
        assertThat(patientRepository.findById(newPatientId)).isPresent();
    }

    @Test
    @DisplayName("Should return 404 when patient ID does not exist")
    void getPatientById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/patients/9999")
                        .header("medilabo-solutions-correlation-id", "test-correlation-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existing patient")
    void updatePatient_WhenNotExists_ShouldReturn404() throws Exception {
        PatientRequestDto updateRequest = new PatientRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setBirthDate(LocalDate.of(1985, 5, 15));
        updateRequest.setGender("F");
        updateRequest.setAddress("Some Address");
        updateRequest.setPhone("555-555-5555");

        mockMvc.perform(put("/patients/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }
}
