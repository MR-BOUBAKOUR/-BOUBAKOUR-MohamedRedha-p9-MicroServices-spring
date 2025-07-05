package com.MedilaboSolutions.gateway.integration;

import com.MedilaboSolutions.gateway.config.AbstractPostgresContainerTest;
import com.MedilaboSolutions.gateway.dto.AuthRequest;
import com.MedilaboSolutions.gateway.util.TokenTestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "server.ssl.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
public class GatewayIT extends AbstractPostgresContainerTest {

    @LocalServerPort
    private int port;

    private RequestSpecification requestSpec;

    @BeforeEach
    void setUp() {
        // Prepare the specifications of the request so that we don't repeat it across our tests
        requestSpec = given()
                .port(port)
                .contentType(ContentType.JSON)
                // Log the request and response only if an assertion fails.
                .log().ifValidationFails();
    }

    @Nested
    class AuthenticationFlowIT {

        @Test
        @DisplayName("Classic Login Returns Access Token And Refresh Cookie")
        void classicLogin_WithValidCredentials_ReturnsAccessTokenAndRefreshCookie() {
            AuthRequest loginRequest = new AuthRequest("prenom_medecin", "123");

            ValidatableResponse response = requestSpec
                    .body(loginRequest)
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(200)
                    .body("accessToken", notNullValue())
                    .body("expiresIn", greaterThan(0))
                    .body("email", equalTo("medecin.medilabosolutions@gmail.com"))
                    .body("username", equalTo("prenom_medecin"))
                    .cookie("refreshToken");

            // Additional check to ensure the token is not empty or just whitespace
            String accessToken = response.extract().path("accessToken");
            assertThat(accessToken).isNotBlank();
        }

        @Test
        @DisplayName("Classic Login With Invalid Credentials Returns 401")
        void classicLogin_WithInvalidCredentials_Returns401() {
            AuthRequest loginRequest = new AuthRequest("prenom_medecin", "wrong_password");

            requestSpec
                    .body(loginRequest)
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Refresh Token Flow Returns New Access Token")
        void refreshTokenFlow_WithValidRefreshToken_ReturnsNewAccessToken() {
            // First login to get refresh token
            AuthRequest loginRequest = new AuthRequest("prenom_medecin", "123");
            String refreshToken = requestSpec
                    .body(loginRequest)
                    .post("/login")
                    .then()
                    .statusCode(200)
                    .extract()
                    .cookie("refreshToken");

            // Use refresh token to get new access token
            requestSpec
                    .cookie("refreshToken", refreshToken)
                    .when()
                    .post("/refresh")
                    .then()
                    .statusCode(200)
                    .body("accessToken", notNullValue())
                    .body("expiresIn", greaterThan(0))
                    .body("username", equalTo("prenom_medecin"));
        }

        @Test
        @DisplayName("Logout Clears Tokens And Removes Refresh Cookie")
        void logout_WhenCalled_ClearsTokensAndRemovesRefreshCookie() {

            Response response = requestSpec
                    .when()
                    .post("/logout")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            List<String> setCookies = response.getHeaders().getValues("Set-Cookie");

            // Calling logout clears the refreshToken cookie by setting its Max-Age to 0
            boolean refreshTokenExpired = setCookies.stream()
                    .anyMatch(cookie -> cookie.startsWith("refreshToken=") && cookie.contains("Max-Age=0"));

            assertThat(refreshTokenExpired).isTrue();
        }
    }

    @Nested
    class AuthenticationFailureIT {
        @Test
        @DisplayName("Unauthorized Request Returns 401")
        void accessProtectedEndpoint_WithoutAuth_Returns401() {
            requestSpec
                    .when()
                    .get("/api/patients")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Expired Token Returns 401")
        void accessProtectedEndpoint_WithExpiredToken_Returns401() {
            String expiredToken = TokenTestHelper.expiredAccessToken();

            requestSpec
                    .header("Authorization", "Bearer " + expiredToken)
                    .when()
                    .get("/api/patients")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Invalid Token Returns 401")
        void accessProtectedEndpoint_WithInvalidToken_Returns401() {
            String invalidToken = TokenTestHelper.invalidToken();

            requestSpec
                    .header("Authorization", "Bearer " + invalidToken)
                    .when()
                    .get("/api/patients")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Refresh With Missing Cookie Returns 401")
        void refresh_WithoutRefreshToken_Returns401() {
            requestSpec
                    .when()
                    .post("/refresh")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Refresh With Invalid Cookie Returns 401")
        void refresh_WithInvalidRefreshToken_Returns401() {
            String invalidToken = TokenTestHelper.invalidToken();

            requestSpec
                    .cookie("refreshToken", invalidToken)
                    .when()
                    .post("/refresh")
                    .then()
                    .statusCode(401);
        }
    }
}