package com.MedilaboSolutions.assessment.integration;

import com.MedilaboSolutions.assessment.config.AbstractContainerTest;
import com.MedilaboSolutions.assessment.config.RabbitMQConfig;
import com.MedilaboSolutions.assessment.config.TestAiConfig;
import com.MedilaboSolutions.assessment.dto.*;
import com.MedilaboSolutions.assessment.service.AssessmentService;
import com.MedilaboSolutions.assessment.service.client.NoteFeignClient;
import com.MedilaboSolutions.assessment.service.client.PatientFeignClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
@ActiveProfiles("test")
@Import(TestAiConfig.class)
class AssessmentIT extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientFeignClient patientFeignClient;

    @MockitoBean
    private NoteFeignClient noteFeignClient;

    private static final String CORRELATION_ID = "test-correlation-id";
    private static final Long PATIENT_ID = 1L;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        assessmentService.deleteAllAssessments();

        PatientDto testPatient = createTestPatient();
        List<NoteDto> testNotes = List.of(createTestNote());

        when(patientFeignClient.getPatientById(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", testPatient)));

        when(noteFeignClient.getNoteByPatientId(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok(new SuccessResponse<>(200, "Success", testNotes)));
    }

    @Test
    @DisplayName("Should create manual assessment and send notification event")
    void shouldCreateManualAssessmentAndSendNotificationEvent() throws Exception {
        // Given
        AssessmentCreateDto createDto = new AssessmentCreateDto();
        createDto.setLevel("HIGH");
        createDto.setContext(List.of("Le patient présente des niveaux élevés de glucose"));
        createDto.setAnalysis("L'évaluation manuelle indique un risque élevé de diabète");
        createDto.setRecommendations(List.of("Consultation médicale immédiate requise"));

        // When & Then
        Integer assessmentIdInt =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
                .contentType(ContentType.JSON)
                .body(createDto)
            .when()
                .post("/assessments/patient/{patientId}/create", PATIENT_ID)
            .then()
                .statusCode(201)
                .body("status", equalTo(201))
                .body("message", equalTo("Assessment created successfully"))
                .body("data.patId", equalTo(PATIENT_ID.intValue()))
                .body("data.level", equalTo("HIGH"))
                .body("data.status", equalTo("MANUAL"))
                .body("data.analysis", equalTo("L'évaluation manuelle indique un risque élevé de diabète"))
                .extract()
                .path("data.id");

        Long assessmentId = assessmentIdInt.longValue();

        // Verify that the notification is sent
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(RabbitMQConfig.NOTIFICATION_QUEUE_NAME);
            assertThat(message).isNotNull();

            AssessmentReportReadyEvent event = objectMapper.readValue(
                    message.getBody(), AssessmentReportReadyEvent.class
            );
            assertThat(event.getAssessmentId()).isEqualTo(assessmentId);
            assertThat(event.getPatientId()).isEqualTo(PATIENT_ID);
        });
    }

    @Test
    @DisplayName("Should queue AI assessment and process it asynchronously")
    void shouldQueueAiAssessmentAndProcessAsynchronously() {
        // When & Then
        Integer assessmentIdInt =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .post("/assessments/patient/{patientId}/queue", PATIENT_ID)
            .then()
                .statusCode(201)
                .body("status", equalTo(201))
                .body("message", equalTo("Assessment created and queued successfully"))
                .body("data.patId", equalTo(PATIENT_ID.intValue()))
                .body("data.status", equalTo("QUEUED"))
                .extract()
                .path("data.id");

        Long assessmentId = assessmentIdInt.longValue();

        // Wait for asynchronous processing
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .get("/assessments/{assessmentId}", assessmentId)
            .then()
                .statusCode(200)
                .body("data.status", anyOf(equalTo("PROCESSING"), equalTo("PENDING")));
        });
    }

    @Test
    @DisplayName("Should get assessments by patient ID")
    void shouldGetAssessmentsByPatientId() {

        AssessmentCreateDto createDto1 = new AssessmentCreateDto();
        createDto1.setLevel("LOW");
        createDto1.setAnalysis("First assessment");

        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .contentType(ContentType.JSON)
            .body(createDto1)
        .when()
            .post("/assessments/patient/{patientId}/create", PATIENT_ID);

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .get("/assessments/patient/{patientId}", PATIENT_ID)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessments fetched successfully"))
            .body("data", hasSize(1))
            .body("data[0].patId", equalTo(PATIENT_ID.intValue()));
    }

    @Test
    @DisplayName("Should get specific assessment by ID")
    void shouldGetAssessmentById() {
        // Given
        AssessmentCreateDto createDto = new AssessmentCreateDto();
        createDto.setLevel("MEDIUM");
        createDto.setAnalysis("Test analysis");

        Integer assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
                .contentType(ContentType.JSON)
                .body(createDto)
            .when()
                .post("/assessments/patient/{patientId}/create", PATIENT_ID)
            .then()
                .extract()
                .path("data.id");

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .get("/assessments/{assessmentId}", assessmentId)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessment fetched successfully"))
            .body("data.id", equalTo(assessmentId))
            .body("data.level", equalTo("MEDIUM"))
            .body("data.analysis", equalTo("Test analysis"))
            .body("data.status", equalTo("MANUAL"));
    }

    @Test
    @DisplayName("Should update existing assessment when status allows modifications")
    void shouldUpdateExistingAssessment() {
        // Given - Create AI queued assessment
        Long assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .post("/assessments/patient/{patientId}/queue", PATIENT_ID)
            .then()
                .extract()
                .<Integer>path("data.id").longValue();

        // Wait until status becomes PENDING
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .get("/assessments/{assessmentId}", assessmentId)
            .then()
                .body("data.status", equalTo("PENDING"));
        });

        // When - Update the assessment
        AssessmentDto updateDto = new AssessmentDto();
        updateDto.setLevel("HIGH");
        updateDto.setAnalysis("Analyse mise à jour basée sur les nouvelles données");

        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .contentType(ContentType.JSON)
            .body(updateDto)
        .when()
            .patch("/assessments/{assessmentId}", assessmentId)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessment updated successfully"))
            .body("data.level", equalTo("HIGH"))
            .body("data.analysis", equalTo("Analyse mise à jour basée sur les nouvelles données"))
            .body("data.status", equalTo("UPDATED"));
    }

    @Test
    @DisplayName("Should accept assessment")
    void shouldAcceptAssessment() {
        // Given - Create AI queued assessment and wait for PENDING status
        Long assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .post("/assessments/patient/{patientId}/queue", PATIENT_ID)
            .then()
                .extract()
                .<Integer>path("data.id").longValue();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .get("/assessments/{assessmentId}", assessmentId)
            .then()
                .body("data.status", equalTo("PENDING"));
        });

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .patch("/assessments/{assessmentId}/accept", assessmentId)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessment accepted"))
            .body("data.status", equalTo("ACCEPTED"));
    }

    @Test
    @DisplayName("Should refuse pending assessment")
    void shouldRefusePendingAssessment() {
        // Given
        Long assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .post("/assessments/patient/{patientId}/queue", PATIENT_ID)
            .then()
                .extract()
                .<Integer>path("data.id").longValue();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .get("/assessments/{assessmentId}", assessmentId)
            .then()
                .body("data.status", equalTo("PENDING"));
        });

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .patch("/assessments/{assessmentId}/refuse-pending", assessmentId)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessment pending refused"))
            .body("data.status", equalTo("REFUSED_PENDING"));
    }

    @Test
    @DisplayName("Should refuse assessment")
    void shouldRefuseAssessment() {
        // Given
        Long assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .post("/assessments/patient/{patientId}/queue", PATIENT_ID)
            .then()
                .extract()
                .<Integer>path("data.id").longValue();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
            .when()
                .get("/assessments/{assessmentId}", assessmentId)
            .then()
                .body("data.status", equalTo("PENDING"));
        });

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .patch("/assessments/{assessmentId}/refuse", assessmentId)
        .then()
            .statusCode(200)
            .body("status", equalTo(200))
            .body("message", equalTo("Assessment refused"))
            .body("data.status", equalTo("REFUSED"));
    }

    @Test
    @DisplayName("Should download assessment PDF")
    void shouldDownloadAssessmentPdf() {
        // Given
        AssessmentCreateDto createDto = new AssessmentCreateDto();
        createDto.setLevel("HIGH");
        createDto.setAnalysis("Test analysis for PDF");

        Long assessmentId =
            given()
                .header("medilabo-solutions-correlation-id", CORRELATION_ID)
                .contentType(ContentType.JSON)
                .body(createDto)
            .when()
                .post("/assessments/patient/{patientId}/create", PATIENT_ID)
            .then()
                .extract()
                .<Integer>path("data.id").longValue();

        // When & Then
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .get("/assessments/{assessmentId}/download", assessmentId)
        .then()
            .statusCode(200)
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", containsString("assessment_" + assessmentId + ".pdf"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent assessment")
    void shouldReturn404ForNonExistentAssessment() {
        given()
            .header("medilabo-solutions-correlation-id", CORRELATION_ID)
        .when()
            .get("/assessments/{assessmentId}", 99999L)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 500 for invalid correlation ID header")
    void shouldReturn400ForMissingCorrelationId() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/assessments/patient/{patientId}", PATIENT_ID)
        .then()
            .statusCode(500);
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
        note.setNote("Le patient présente des signes de facteurs de risque du diabète, y compris un taux de glucose élevé.");
        return note;
    }
}