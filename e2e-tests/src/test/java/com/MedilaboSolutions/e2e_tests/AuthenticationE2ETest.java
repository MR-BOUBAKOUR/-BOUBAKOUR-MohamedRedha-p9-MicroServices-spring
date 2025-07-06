package com.MedilaboSolutions.e2e_tests;

import com.MedilaboSolutions.e2e_tests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestPropertySource(properties = {
        "server.ssl.enabled=false",
        "logging.level.io.restassured=DEBUG",
        "logging.level.com.MedilaboSolutions.e2e_tests=DEBUG"
})
public class AuthenticationE2ETest {

}
