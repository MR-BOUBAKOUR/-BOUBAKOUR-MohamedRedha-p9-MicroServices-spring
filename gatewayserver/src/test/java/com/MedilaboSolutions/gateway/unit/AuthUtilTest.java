package com.MedilaboSolutions.gateway.unit;

import com.MedilaboSolutions.gateway.utils.AuthUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

    private AuthUtil authUtil;

    @BeforeEach
    void setUp() {
        // Create an AuthUtil instance and modify its private fields via reflection
        // only for testing purposes, without affecting the actual production code.
        authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "test-secret-key-minimum-256-bits-for-hs256-algorithm");
        ReflectionTestUtils.setField(authUtil, "accessTokenExpirationMs", 10_000L); // 10 seconds
        ReflectionTestUtils.setField(authUtil, "refreshTokenExpirationMs", 60_000L); // 1 minute
    }

    @Test
    @DisplayName("Should generate valid access token with all claims")
    void shouldGenerateValidAccessToken() {
        // Given
        String username = "username_test";
        String role = "ROLE_MEDECIN";
        String imageUrl = "https://example.com/image.jpg";

        // When
        String token = authUtil.generateAccessToken(username, role, imageUrl);

        // Then
        assertThat(token).isNotBlank();

        Claims claims = authUtil.getAllClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("username", String.class)).isEqualTo(username);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.get("image_url", String.class)).isEqualTo(imageUrl);
        assertThat(claims.get("token_type", String.class)).isEqualTo("access");

        // We test that the token's expiration time is set to about 10 seconds after its creation,
        // with a 5-second margin of error.
        long expectedExpiration = System.currentTimeMillis() + 10_000L; // 10 seconds
        assertThat(claims.getExpiration().getTime())
                .isCloseTo(expectedExpiration, within(5000L)); // 5 second tolerance
    }

    @Test
    @DisplayName("Should generate access token without image URL when null")
    void shouldGenerateAccessTokenWithoutImageUrl() {
        // Given
        String username = "username_test";
        String role = "ROLE_MEDECIN";

        // When
        String token = authUtil.generateAccessToken(username, role, null);

        // Then
        Claims claims = authUtil.getAllClaimsFromToken(token);
        assertThat(claims.get("image_url")).isNull();
    }

    @Test
    @DisplayName("Should validate correct token")
    void shouldValidateCorrectToken() {
        // Given
        String token = authUtil.generateAccessToken("user", "ROLE_MEDECIN", null);

        // When & Then
        assertThat(authUtil.isValidToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When & Then
        assertThat(authUtil.isValidToken(malformedToken)).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        AuthUtil shortExpirationAuthUtil = new AuthUtil();
        ReflectionTestUtils.setField(shortExpirationAuthUtil, "secretKey", "test-secret-key-minimum-256-bits-for-hs256-algorithm");
        ReflectionTestUtils.setField(shortExpirationAuthUtil, "accessTokenExpirationMs", 1L); // 1ms

        String token = shortExpirationAuthUtil.generateAccessToken("user", "ROLE_MEDECIN", null);

        await()
                .pollDelay(Duration.ofMillis(50))                               // wait before first check
                .atMost(Duration.ofMillis(100))                                 // max time to wait
                .until(() -> !shortExpirationAuthUtil.isValidToken(token));     // wait until token is invalid

        assertThat(shortExpirationAuthUtil.isValidToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should validate refresh token type correctly")
    void shouldValidateRefreshTokenType() {
        // Given
        String refreshToken = authUtil.generateRefreshToken("user");
        String accessToken = authUtil.generateAccessToken("user", "ROLE_MEDECIN", null);

        // When & Then
        assertThat(authUtil.isValidRefreshTokenType(refreshToken)).isTrue();
        assertThat(authUtil.isValidRefreshTokenType(accessToken)).isFalse();
    }
}
