package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.controller.AssessmentController;
import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.PdfService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentService assessmentService;

    @MockitoBean
    private PdfService pdfService;

    @Autowired
    private ObjectMapper objectMapper;

    private AssessmentDto assessmentDto;
    private AssessmentCreateDto createDto;

    @BeforeEach
    void setUp() {
        assessmentDto = new AssessmentDto();
        assessmentDto.setId(1L);
        assessmentDto.setPatId(1L);
        assessmentDto.setLevel("MODERATE");
        assessmentDto.setStatus("PENDING");

        createDto = new AssessmentCreateDto();
        createDto.setLevel("MODERATE");
        createDto.setAnalysis("Test analysis");
    }

    @Test
    @DisplayName("Should fetch assessments by patient ID")
    void getAssessmentsByPatientId_ShouldReturnList() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AssessmentDto> page = new PageImpl<>(List.of(assessmentDto), pageable, 1);

        when(assessmentService.findByPatientId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/assessments/patient/1")
                        .header("medilabo-solutions-correlation-id", "test-corr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Assessments fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));

        verify(assessmentService).findByPatientId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should fetch assessment by ID")
    void getAssessmentById_ShouldReturnAssessment() throws Exception {
        when(assessmentService.findAssessmentById(1L)).thenReturn(assessmentDto);

        mockMvc.perform(get("/assessments/1")
                        .header("medilabo-solutions-correlation-id", "test-corr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Assessment fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(assessmentService).findAssessmentById(1L);
    }

    @Test
    @DisplayName("Should queue AI assessment for patient")
    void queueAiAssessment_ShouldReturnAssessment() throws Exception {
        when(assessmentService.queueAiAssessmentForProcessing(1L, "test-corr"))
                .thenReturn(assessmentDto);

        mockMvc.perform(post("/assessments/patient/1/queue")
                        .header("medilabo-solutions-correlation-id", "test-corr"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Assessment created and queued successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(assessmentService).queueAiAssessmentForProcessing(1L, "test-corr");
    }

    @Test
    @DisplayName("Should create assessment for patient")
    void createAssessment_ShouldReturnAssessment() throws Exception {
        when(assessmentService.createAssessment(eq(1L), any(AssessmentCreateDto.class), eq("test-corr")))
                .thenReturn(assessmentDto);

        mockMvc.perform(post("/assessments/patient/1/create")
                        .header("medilabo-solutions-correlation-id", "test-corr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Assessment created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(assessmentService).createAssessment(eq(1L), any(AssessmentCreateDto.class), eq("test-corr"));
    }

    @Test
    @DisplayName("Should update assessment")
    void updateAssessment_ShouldReturnUpdated() throws Exception {
        when(assessmentService.updateAssessment(eq(1L), any(AssessmentDto.class), eq("test-corr")))
                .thenReturn(assessmentDto);

        mockMvc.perform(patch("/assessments/1")
                        .header("medilabo-solutions-correlation-id", "test-corr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assessmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Assessment updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(assessmentService).updateAssessment(eq(1L), any(AssessmentDto.class), eq("test-corr"));
    }

    @Test
    @DisplayName("Should generate PDF for assessment")
    void downloadAssessmentPdf_ShouldReturnPdfBytes() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(pdfService.generatePdfAssessment(1L, "test-corr")).thenReturn(pdfBytes);

        mockMvc.perform(get("/assessments/1/download")
                        .header("medilabo-solutions-correlation-id", "test-corr"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=assessment_1.pdf"))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(pdfBytes));

        verify(pdfService).generatePdfAssessment(1L, "test-corr");
    }
}
