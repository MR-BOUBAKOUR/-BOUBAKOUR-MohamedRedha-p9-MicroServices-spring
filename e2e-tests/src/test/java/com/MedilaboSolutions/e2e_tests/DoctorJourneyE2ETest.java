package com.MedilaboSolutions.e2e_tests;

import com.MedilaboSolutions.e2e_tests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = {
        "server.ssl.enabled=false",
        "logging.level.io.restassured=DEBUG",
        "logging.level.com.MedilaboSolutions.e2e_tests=DEBUG"
})
public class DoctorJourneyE2ETest {

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

        // Waiting for all our services to be up & healthy
        waitForServicesReady();
    }

    @Test
    void shouldCompleteFullDoctorJourney() {

        authenticateDoctor();

        createPatient();

        updatePatient();

        createNoteForPatient();

        assessPatientRisk();

        verifyPatientDataConsistency();
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

    private void authenticateDoctor() {

        String accessToken = requestSpec
                .body("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(TestConfig.TEST_USER_USERNAME, TestConfig.TEST_USER_PASSWORD))
                .post(TestConfig.LOGIN_API)
                .then()
                .statusCode(200)
                .cookie("refreshToken", notNullValue())
                .extract()
                .path("accessToken");

        requestSpec = requestSpec.header("Authorization", "Bearer " + accessToken);
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

        System.out.println("Created patient with ID: " + createdPatientId);
    }

    private void updatePatient() {
        requestSpec
                .body("""
                {
                    "id": %d,
                    "firstName": "John",
                    "lastName": "Smith",
                    "birthDate": "1990-01-01",
                    "gender": "M",
                    "address": "456 Updated Street",
                    "phone": "555-123-4567"
                }
                """.formatted(createdPatientId))
                .put(TestConfig.PATIENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.lastName", equalTo("Smith"))
                .body("data.address", equalTo("456 Updated Street"));

        System.out.println("Updated patient with ID: " + createdPatientId);
    }

    private void createNoteForPatient() {
        createdNoteId = requestSpec
                .body("""
                {
                    "patId": %d,
                    "note": "Patient présente des symptômes de diabète"
                }
                """.formatted(createdPatientId))
                .post(TestConfig.NOTES_API)
                .then()
                .statusCode(201)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.note", containsString("diabète"))
                .extract()
                .path("data.id");

        System.out.println("Created note with ID: " + createdNoteId);
    }

    private void assessPatientRisk() {
        // Waiting for the data update across microservices
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

        requestSpec
                .get(TestConfig.ASSESSMENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.patId", equalTo(createdPatientId))
                .body("data.assessmentResult", notNullValue());

        System.out.println("Assessment completed for patient: " + createdPatientId);
    }

    private void verifyPatientDataConsistency() {
        requestSpec
                .get(TestConfig.PATIENTS_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data.firstName", equalTo("John"))
                .body("data.lastName", equalTo("Smith"))
                .body("data.address", equalTo("456 Updated Street"))
                .body("data.gender", equalTo("M"))
                .body("data.birthDate", equalTo("1990-01-01"));

        requestSpec
                .get(TestConfig.NOTES_API + "/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("data[0].id", equalTo(createdNoteId))
                .body("data[0].patId", equalTo(createdPatientId))
                .body("data[0].note", containsString("diabète"));

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

        System.out.println("✅ Global consistency verified for patient ID: " + createdPatientId);
    }

    @AfterEach
    void tearDown() {
        if (createdNoteId != null) {
            // Deleting the added note of the patient
            requestSpec
                    .delete(TestConfig.NOTES_API + "/" + createdPatientId)
                    .then()
                    .statusCode(anyOf(is(204), is(404)));
        }

        if (createdPatientId != null) {
            // Deleting the patient
            requestSpec
                    .delete(TestConfig.PATIENTS_API + "/" + createdPatientId)
                    .then()
                    .statusCode(anyOf(is(204), is(404)));
        }

        System.out.println("Cleaned up test data for patient ID: " + createdPatientId);
    }

}
