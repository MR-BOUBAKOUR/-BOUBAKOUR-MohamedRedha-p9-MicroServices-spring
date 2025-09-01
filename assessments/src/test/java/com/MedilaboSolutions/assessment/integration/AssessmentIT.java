//package com.MedilaboSolutions.assessment.integration;
//
//import com.MedilaboSolutions.assessment.config.AbstractRabbitMQContainerTest;
//import com.MedilaboSolutions.assessment.config.RabbitMQConfig;
//import com.MedilaboSolutions.assessment.dto.*;
//import com.MedilaboSolutions.assessment.service.AssessmentService;
//import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
//import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.awaitility.Awaitility.await;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@TestPropertySource(properties = {
//        "eureka.client.enabled=false",
//        "eureka.client.register-with-eureka=false",
//        "eureka.client.fetch-registry=false"
//})
//class AssessmentIT extends AbstractRabbitMQContainerTest {
//
//    @Autowired
//    private AssessmentService assessmentService;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private PatientFeignClient patientFeignClient;
//
//    @MockitoBean
//    private NoteFeignClient noteFeignClient;
//
//    @Test
//    @DisplayName("Should publish HighRiskAssessmentEvent to RabbitMQ when assessment is 'Early onset'")
//    void shouldPublishHighRiskEvent_WhenEarlyOnsetAssessment() {
//        // Given
//        PatientDto patient = createPatient(1L, "John", "Doe", "M", 40, false);
//        when(patientFeignClient.getPatientById(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", patient)));
//
//        List<NoteDto> notes = createNotesWithTriggers(8); // 8 triggers = Early onset
//        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", notes)));
//
//        // When
//        AssessmentDto result = assessmentService.generateAssessment(1L, "test-correlation-id");
//
//        // Then
//        assertThat(result.getLevel()).isEqualTo("Early onset");
//
//        // Verify message was published to RabbitMQ
//        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
//            Message message = rabbitTemplate.receive(RabbitMQConfig.NOTIFICATION_QUEUE_NAME, 1000);
//            assertThat(message).isNotNull();
//
//            HighRiskAssessmentEvent event = objectMapper.readValue(message.getBody(), HighRiskAssessmentEvent.class);
//            assertThat(event.getPatId()).isEqualTo(1L);
//            assertThat(event.getPatFirstName()).isEqualTo("John");
//            assertThat(event.getPatLastname()).isEqualTo("Doe");
//            assertThat(event.getRiskLevel()).isEqualTo("Early onset");
//        });
//
//        // Verify patient mail flag was updated
//        verify(patientFeignClient).updateEarlyOnsetMailSent(1L, true, "test-correlation-id");
//    }
//
//    @Test
//    @DisplayName("Should not publish event when assessment is not 'Early onset'")
//    void shouldNotPublishEvent_WhenNotEarlyOnset() {
//        // Given
//        PatientDto patient = createPatient(1L, "Jane", "Smith", "F", 35, false);
//        when(patientFeignClient.getPatientById(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", patient)));
//
//        List<NoteDto> notes = createNotesWithTriggers(2); // 2 triggers = Borderline
//        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", notes)));
//
//        // When
//        AssessmentDto result = assessmentService.generateAssessment(1L, "test-correlation-id");
//
//        // Then
//        assertThat(result.getLevel()).isEqualTo("Borderline");
//
//        // Verify no message in queue
//        Message message = rabbitTemplate.receive(RabbitMQConfig.NOTIFICATION_QUEUE_NAME, 1000);
//        assertThat(message).isNull();
//
//        // Verify patient mail flag was not updated
//        verify(patientFeignClient, never()).updateEarlyOnsetMailSent(anyLong(), anyBoolean(), anyString());
//    }
//
//    @Test
//    @DisplayName("Should not publish duplicate event when patient already has mail sent flag")
//    void shouldNotPublishDuplicateEvent_WhenMailAlreadySent() {
//        // Given
//        PatientDto patient = createPatient(1L, "John", "Doe", "M", 40, true); // Mail already sent
//        when(patientFeignClient.getPatientById(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", patient)));
//
//        List<NoteDto> notes = createNotesWithTriggers(8); // 8 triggers = Early onset
//        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", notes)));
//
//        // When
//        AssessmentDto result = assessmentService.generateAssessment(1L, "test-correlation-id");
//
//        // Then
//        assertThat(result.getLevel()).isEqualTo("Early onset");
//
//        // Verify no message in queue (no duplicate)
//        Message message = rabbitTemplate.receive(RabbitMQConfig.NOTIFICATION_QUEUE_NAME, 1000);
//        assertThat(message).isNull();
//
//        // Verify patient mail flag was not updated again
//        verify(patientFeignClient, never()).updateEarlyOnsetMailSent(anyLong(), anyBoolean(), anyString());
//    }
//
//    @Test
//    @DisplayName("Should reset mail flag when assessment changes from 'Early onset' to lower risk")
//    void shouldResetMailFlag_WhenRiskLevelDecreases() {
//        // Given
//        PatientDto patient = createPatient(1L, "John", "Doe", "M", 40, true);  // Mail already sent
//        when(patientFeignClient.getPatientById(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", patient)));
//
//        List<NoteDto> notes = createNotesWithTriggers(2); // 2 triggers = Borderline
//        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
//                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "ok", notes)));
//
//        // When
//        AssessmentDto result = assessmentService.generateAssessment(1L, "test-correlation-id");
//
//        // Then
//        assertThat(result.getLevel()).isEqualTo("Borderline");
//
//        // Verify patient mail flag was reset
//        verify(patientFeignClient).updateEarlyOnsetMailSent(1L, false, "test-correlation-id");
//    }
//
//    private PatientDto createPatient(Long id, String firstName, String lastName, String gender, int ageYears, boolean mailSent) {
//        PatientDto patient = new PatientDto();
//        patient.setId(id);
//        patient.setFirstName(firstName);
//        patient.setLastName(lastName);
//        patient.setGender(gender);
//        patient.setBirthDate(LocalDate.now().minusYears(ageYears));
//        patient.setEarlyOnsetMailSent(mailSent);
//        return patient;
//    }
//
//    private List<NoteDto> createNotesWithTriggers(int triggerCount) {
//        List<String> triggers = List.of("Hémoglobine A1C", "Microalbumine", "Taille", "Poids",
//                "Fumeur", "Anormal", "Cholestérol", "Vertiges", "Rechute", "Réaction");
//
//        return triggers.stream()
//                .limit(triggerCount)
//                .map(trigger -> new NoteDto(null, 1L, "John", "Trigger : " + trigger))
//                .toList();
//    }
//}