package com.MedilaboSolutions.assessment.unit;

import com.MedilaboSolutions.assessment.config.RabbitMQConfig;
import com.MedilaboSolutions.assessment.controller.AssessmentSseController;
import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.exception.ResourceNotFoundException;
import com.MedilaboSolutions.assessment.mapper.AssessmentMapper;
import com.MedilaboSolutions.assessment.model.Assessment;
import com.MedilaboSolutions.assessment.repository.AssessmentRepository;
import com.MedilaboSolutions.assessment.service.AiAssessmentService;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private PatientFeignClient patientFeignClient;
    @Mock
    private NoteFeignClient noteFeignClient;
    @Mock
    private AiAssessmentService aiAssessmentService;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private AssessmentMapper assessmentMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private AssessmentSseController sseController;
    @Mock
    private ObjectMapper objectMapper;

    private AssessmentService assessmentService;

    @BeforeEach
    void setUp() throws Exception {

        // Mocking the JSON file
        Map<String, String> mockRefs = Map.of(
                "[ref-123]", "Document médical de référence"
        );
        when(objectMapper.readValue(any(java.io.InputStream.class), eq(Map.class)))
                .thenReturn(mockRefs);

        assessmentService = new AssessmentService(
                patientFeignClient,
                noteFeignClient,
                aiAssessmentService,
                assessmentRepository,
                assessmentMapper,
                rabbitTemplate,
                sseController,
                new ClassPathResource("docs/guidelines_result_refs.json"),
                objectMapper
        );
    }

    @Test
    @DisplayName("Should find assessments by patient ID with pagination")
    void shouldFindAssessmentsByPatientIdWithPagination() {
        // Given
        Long patId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Assessment assessment = createTestAssessment();
        AssessmentDto expectedDto = createTestAssessmentDto();

        Page<Assessment> assessmentPage = new PageImpl<>(List.of(assessment));

        when(assessmentRepository.findByPatIdAndStatusIn(eq(patId), anyList(), eq(pageable)))
                .thenReturn(assessmentPage);
        when(assessmentMapper.toAssessmentDto(assessment)).thenReturn(expectedDto);

        // When
        Page<AssessmentDto> result = assessmentService.findByPatientId(patId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Should find assessment by ID")
    void shouldFindAssessmentById() {
        // Given
        Long assessmentId = 1L;
        Assessment assessment = createTestAssessment();
        AssessmentDto expectedDto = createTestAssessmentDto();

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentMapper.toAssessmentDto(assessment)).thenReturn(expectedDto);

        // When
        AssessmentDto result = assessmentService.findAssessmentById(assessmentId);

        // Then
        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Should throw exception when assessment not found")
    void shouldThrowExceptionWhenAssessmentNotFound() {
        // Given
        Long assessmentId = 999L;
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assessmentService.findAssessmentById(assessmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Assessment not found with id 999");
    }

    @Test
    @DisplayName("Should create manual assessment")
    void shouldCreateManualAssessment() {
        // Given
        Long patId = 1L;
        String correlationId = "test-correlation";
        AssessmentCreateDto createDto = new AssessmentCreateDto();
        createDto.setLevel("HIGH");
        createDto.setAnalysis("Test analysis");

        Assessment assessment = createTestAssessment();
        Assessment savedAssessment = createTestAssessment();
        savedAssessment.setId(1L);
        AssessmentDto expectedDto = createTestAssessmentDto();

        when(assessmentMapper.toAssessment(createDto)).thenReturn(assessment);
        when(assessmentRepository.save(assessment)).thenReturn(savedAssessment);
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(savedAssessment));
        when(assessmentMapper.toAssessmentDto(savedAssessment)).thenReturn(expectedDto);

        // When
        AssessmentDto result = assessmentService.createAssessment(patId, createDto, correlationId);

        // Then
        assertThat(result).isEqualTo(expectedDto);
        verify(assessmentRepository, times(2)).save(any(Assessment.class)); // save + updateStatus
        verify(rabbitTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should update an existing assessment with all fields")
    void shouldUpdateAssessment() {
        // Given
        Long assessmentId = 1L;
        String correlationId = "test-correlation";

        Assessment existingAssessment = createTestAssessment();
        existingAssessment.setId(assessmentId);

        AssessmentDto updatedDto = new AssessmentDto();
        updatedDto.setLevel("HIGH");
        updatedDto.setAnalysis("Updated medical analysis");
        updatedDto.setContext(List.of("New context line 1", "New context line 2"));
        updatedDto.setRecommendations(List.of("New recommendation 1", "New recommendation 2"));
        updatedDto.setSources(List.of("New source 1", "New source 2"));

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(existingAssessment));

        // When
        assessmentService.updateAssessment(assessmentId, updatedDto, correlationId);

        assertThat(existingAssessment.getLevel()).isEqualTo("HIGH");
        assertThat(existingAssessment.getAnalysis()).isEqualTo("Updated medical analysis");
        assertThat(existingAssessment.getContext()).isEqualTo(List.of("New context line 1", "New context line 2"));
        assertThat(existingAssessment.getRecommendations()).isEqualTo(List.of("New recommendation 1", "New recommendation 2"));
        assertThat(existingAssessment.getSources()).isEqualTo(List.of("New source 1", "New source 2"));

        // Then
        verify(assessmentRepository, times(2)).save(existingAssessment);
        verify(rabbitTemplate).convertAndSend((String) eq(RabbitMQConfig.NOTIFICATION_QUEUE_NAME), (Object) any());
    }

    @Test
    @DisplayName("Should queue AI assessment")
    void shouldQueueAiAssessment() {
        // Given
        Long patId = 1L;
        String correlationId = "test-correlation";

        Assessment savedAssessment = createTestAssessment();
        savedAssessment.setId(1L);
        AssessmentDto expectedDto = createTestAssessmentDto();

        when(assessmentRepository.save(any(Assessment.class))).thenReturn(savedAssessment);
        when(assessmentRepository.findById(1L)).thenReturn(Optional.of(savedAssessment));
        when(assessmentMapper.toAssessmentDto(savedAssessment)).thenReturn(expectedDto);

        // When
        AssessmentDto result = assessmentService.queueAiAssessmentForProcessing(patId, correlationId);

        // Then
        assertThat(result).isEqualTo(expectedDto);
        verify(rabbitTemplate).convertAndSend(anyString(), any(AiAssessmentProcessEvent.class));
    }

    @Test
    @DisplayName("Should process queued AI assessment successfully")
    void shouldProcessQueuedAiAssessment() {
        // Given
        Long assessmentId = 1L;
        Long patId = 1L;
        String correlationId = "test-correlation";

        Assessment assessment = createTestAssessment();
        assessment.setId(assessmentId);
        assessment.setPatId(patId);

        PatientDto patient = createTestPatient();
        List<NoteDto> notes = List.of(createTestNote());
        AiAssessmentResponse aiResponse = createTestAiResponse();

        AssessmentDto dto = createTestAssessmentDto();
        when(assessmentMapper.toAssessmentDto(any(Assessment.class))).thenReturn(dto);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        when(patientFeignClient.getPatientById(patId, correlationId))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", patient)));

        when(noteFeignClient.getNoteByPatientId(patId, correlationId))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", notes)));

        when(aiAssessmentService.assessDiabetesRisk(anyInt(), anyString(), anyString(), any()))
                .thenReturn(aiResponse);

        // When
        assessmentService.processQueuedAiAssessment(assessmentId, patId, correlationId);

        // Then
        verify(assessmentRepository, atLeast(2)).save(any(Assessment.class)); // processing + final save
        verify(sseController, atLeast(3)).emitAssessmentProgress(anyLong(), anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should reject status update for finalized assessments (ACCEPTED, UPDATED, MANUAL, REFUSED)")
    void shouldRejectStatusUpdateForFinalizedAssessment() {
        // Given
        Long assessmentId = 1L;
        Assessment finalizedAssessment = createTestAssessment();
        finalizedAssessment.setStatus(AssessmentStatus.ACCEPTED);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(finalizedAssessment));

        // When/Then
        assertThatThrownBy(() ->
                assessmentService.updateStatus(assessmentId, AssessmentStatus.PROCESSING, "test")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be modified");
    }

    private Assessment createTestAssessment() {
        Assessment assessment = new Assessment();
        assessment.setId(1L);
        assessment.setPatId(1L);
        assessment.setLevel("MODERATE");
        assessment.setStatus(AssessmentStatus.PENDING);
        assessment.setCreatedAt(Instant.now());
        assessment.setUpdatedAt(Instant.now());
        return assessment;
    }

    private AssessmentDto createTestAssessmentDto() {
        AssessmentDto dto = new AssessmentDto();
        dto.setId(1L);
        dto.setPatId(1L);
        dto.setLevel("MODERATE");
        dto.setStatus("PENDING");
        return dto;
    }

    private PatientDto createTestPatient() {
        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setGender("M");
        return patient;
    }

    private NoteDto createTestNote() {
        NoteDto note = new NoteDto();
        note.setId("1");
        note.setPatId(1L);
        note.setNote("Patient shows signs of diabetes risk factors");
        return note;
    }

    private AiAssessmentResponse createTestAiResponse() {
        return AiAssessmentResponse.builder()
                .level("MODERATE")
                .context("Patient context")
                .analysis("Medical analysis")
                .recommendations("Follow-up recommendations")
                .sources("[[ref-123], page 45]")
                .build();
    }
}