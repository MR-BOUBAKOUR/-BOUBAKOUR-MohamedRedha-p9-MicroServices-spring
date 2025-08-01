package com.MedilaboSolutions.e2e_tests;

import com.MedilaboSolutions.e2e_tests.config.TestConfig;
import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = {
        "server.ssl.enabled=false",
        "logging.level.io.restassured=DEBUG",
        "logging.level.com.MedilaboSolutions.e2e_tests=DEBUG"
})
public class DoctorJourneyE2ETest {

    private static final Logger log = LoggerFactory.getLogger(DoctorJourneyE2ETest.class);

    static {
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

        System.setProperty("TEST_USER_USERNAME", dotenv.get("TEST_USER_USERNAME", ""));
        System.setProperty("TEST_USER_PASSWORD", dotenv.get("TEST_USER_PASSWORD", ""));
    }

    String username = System.getProperty("TEST_USER_USERNAME");
    String password = System.getProperty("TEST_USER_PASSWORD");

    public RequestSpecification requestSpec;

    private Integer createdPatientId;
    private String createdNoteId;

    @BeforeEach
    void setUp() {

        RestAssured.useRelaxedHTTPSValidation();

        requestSpec = given()
                .contentType(ContentType.JSON)
                // Log the request and response only if an assertion fails.
                .log().ifValidationFails();

        // Wait for all services to be up and healthy
        waitForServicesReady();
    }

    @Test
    @DisplayName("Should complete full doctor journey with risk progression: None → Borderline → In Danger → Early onset")
    void shouldCompleteFullDoctorJourneyWith4StepRiskProgression() {

        loginDoctor();

        createPatient();

        String step1Risk = assessPatientRisk();
        assertThat(step1Risk).isEqualTo("None");
        log.info("✅ STEP 1 - Initial risk: {}", step1Risk);

        addNoteWithTriggersForBorderline();

        String step2Risk = assessPatientRisk();
        assertThat(step2Risk).isEqualTo("Borderline");
        log.info("✅ STEP 2 - Risk after adding note1 : {}", step2Risk);

        addNoteWithTriggersForInDanger();

        String step3Risk = assessPatientRisk();
        assertThat(step3Risk).isEqualTo("In Danger");
        log.info("✅ STEP 3 - Risk after adding note2 : {}", step3Risk);

        addNoteWithTriggersForEarlyOnset();
        updatePatient();

        String step4Risk = assessPatientRisk();
        assertThat(step4Risk).isEqualTo("Early onset");
        log.info("✅ STEP 4 - Risk after adding note3 & updating patient: {}", step4Risk);

        waitForNotificationsLog();

        verifyPatientDataConsistency();

        logoutDoctor();
    }

    private void waitForServicesReady() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT))
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> {
                    try {
                        given()
                                .get(TestConfig.HEALTH_API)
                                .then()
                                .statusCode(200);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    private void loginDoctor() {

        String accessToken = requestSpec
                .body("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(username, password))
                .post(TestConfig.LOGIN_API)
                .then()
                .statusCode(200)
                .cookie("refreshToken", notNullValue())
                .extract()
                .path("accessToken");

        requestSpec = requestSpec.header("Authorization", "Bearer " + accessToken);

        log.info("Doctor successfully logged in");
    }

    private void createPatient() {
        createdPatientId = requestSpec
                .body("""
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "birthDate": "1990-01-01",
                    "gender": "M",
                    "address": "123 Test Street",
                    "phone": "555-123-4567"
                }
                """)
                .post(TestConfig.PATIENTS_API)
                .then()
                .statusCode(201)
                .body("data.firstName", equalTo("John"))
                .body("data.lastName", equalTo("Doe"))
                .extract()
                .path("data.id");

        log.info("Patient created - Id{}", createdPatientId);
    }

    private void addNoteWithTriggersForBorderline() {
        // Add 2 triggers to reach BORDERLINE (male >30, triggers>=2)
        createdNoteId = requestSpec
                .body("""
                {
                    "patId": %d,
                    "note": "Patient présente Hémoglobine A1C élevée et problème de Poids notable"
                }
                """.formatted(createdPatientId))
                .post(TestConfig.NOTES_API)
                .then()
                .statusCode(201)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.note", containsString("Hémoglobine A1C"))
                .extract()
                .path("data.id");

        log.info("Note1 added (2 triggers) - Id{}", createdNoteId);
    }

    private void addNoteWithTriggersForInDanger() {
        // Add 4 more triggers (total 6 for male >30 = IN DANGER)
        String createdNote2Id = requestSpec
                .body("""
                {
                    "patId": %d,
                    "note": "Patient Fumeur avec Microalbumine anormale, Cholestérol élevé, Vertiges fréquents"
                }
                """.formatted(createdPatientId))
                .post(TestConfig.NOTES_API)
                .then()
                .statusCode(201)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.note", containsString("Fumeur"))
                .extract()
                .path("data.id");

        log.info("Note2 added (4 more triggers) - Id{}", createdNote2Id);
    }

    private void updatePatient() {
        requestSpec
                .body("""
                {
                    "id": %d,
                    "firstName": "Jane",
                    "lastName": "Smith-TEST",
                    "birthDate": "1999-01-01",
                    "gender": "F",
                    "address": "456 Updated Street",
                    "phone": "555-123-4567"
                }
                """.formatted(createdPatientId))
                .put(TestConfig.PATIENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.firstName", equalTo("Jane"))
                .body("data.lastName", equalTo("Smith-TEST"))
                .body("data.gender", equalTo("F"))
                .body("data.address", equalTo("456 Updated Street"));

        log.info("Patient updated - Id{}", createdPatientId);
    }

    private void addNoteWithTriggersForEarlyOnset() {
        // Add 1 more trigger (total 7 for female <30 = EARLY ONSET)
        String createdNote3Id = requestSpec
                .body("""
                {
                    "patId": %d,
                    "note": "Présence d'Anticorps détectée lors du dernier bilan"
                }
                """.formatted(createdPatientId))
                .post(TestConfig.NOTES_API)
                .then()
                .statusCode(201)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.note", containsString("Anticorps"))
                .extract()
                .path("data.id");

        log.info("Note3 added (1 more trigger) - Id{}", createdNote3Id);
    }

    private String assessPatientRisk() {
        // Wait for data to propagate across services
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    try {
                        requestSpec
                                .get(TestConfig.ASSESSMENTS_API + "/" + createdPatientId)
                                .then()
                                .statusCode(200);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        return requestSpec
                .get(TestConfig.ASSESSMENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.assessmentResult", notNullValue())
                .extract()
                .path("data.assessmentResult");
    }

    private void verifyPatientDataConsistency() {
        // Check patient data consistency
        requestSpec
                .get(TestConfig.PATIENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.firstName", equalTo("Jane"))
                .body("data.lastName", equalTo("Smith-TEST"))
                .body("data.address", equalTo("456 Updated Street"))
                .body("data.gender", equalTo("F"))
                .body("data.birthDate", equalTo("1999-01-01"));

        // Check notes data consistency
        requestSpec
                .get(TestConfig.NOTES_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data", hasSize(3))
                .body("data[0].patId", equalTo(createdPatientId))
                .body("data[1].patId", equalTo(createdPatientId))
                .body("data[2].patId", equalTo(createdPatientId));

        // Check assessment data consistency
        requestSpec
                .get(TestConfig.ASSESSMENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.assessmentResult", anyOf(
                        equalTo("None"),
                        equalTo("Borderline"),
                        equalTo("In Danger"),
                        equalTo("Early onset")
                ));

        log.info("Global data consistency verified for patient Id" + createdPatientId);
    }

    private void logoutDoctor() {
        requestSpec
                .post(TestConfig.LOGOUT_API)
                .then()
                .statusCode(200)
                .header("Set-Cookie", containsString("refreshToken=; Max-Age=0"));

        log.info("Doctor successfully logged out");
    }

    private void waitForNotificationsLog() {
        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    // Start a new process to fetch the logs from the 'notifications' container (full logs without --since)
                    Process process = new ProcessBuilder("docker", "logs", "notifications", "--since", "10s").start();
                    // Open the process output stream to read the logs
                    try (InputStream processOutput = process.getInputStream()) {
                        // Read all the logs from the input stream into a single String
                        String logs = new String(processOutput.readAllBytes());
                        // Check if the logs contain the email sent message and the patient name to avoid false positives
                        boolean found = logs.contains("📧 Email sent to") && logs.contains("Smith-TEST");
                        if (found) {
                            log.info("\uD83D\uDD14 High-risk email successfully sent - Id{}", createdPatientId);
                        }
                        return found;
                    }
                });
    }

    @AfterEach
    void tearDown() {
        if (createdNoteId != null) {
            // Delete all notes for this patient
            requestSpec
                    .delete(TestConfig.NOTES_API + "/" + createdPatientId)
                    .then()
                    .statusCode(anyOf(is(204), is(404)));
        }

        if (createdPatientId != null) {
            // Delete the patient
            requestSpec
                    .delete(TestConfig.PATIENTS_API + "/" + createdPatientId)
                    .then()
                    .statusCode(anyOf(is(204), is(404)));
        }

        log.info("Cleaned up test data for patient Id{}", createdPatientId);

        createdPatientId = null;
        createdNoteId = null;
    }
}
