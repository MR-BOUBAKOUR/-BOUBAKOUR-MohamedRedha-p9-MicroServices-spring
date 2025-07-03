package com.MedilaboSolutions.gateway.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class GatewayIT {

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationFlowTests {

        @Test
        @DisplayName("Classic Login Returns Access Token And Refresh Cookie")
        void classicLogin_WithValidCredentials_ReturnsAccessTokenAndRefreshCookie() {
            // TODO
        }

        @Test
        @DisplayName("OAuth2 Login Returns Access Token And Refresh Cookie")
        void oauth2Login_WithGoogleAccount_ReturnsAccessTokenAndRefreshCookie() {
            // TODO
        }

        @Test
        @DisplayName("Refresh Token Flow Returns New Access Token")
        void refreshTokenFlow_WithValidRefreshToken_ReturnsNewAccessToken() {
            // TODO
        }

        @Test
        @DisplayName("Logout Clears Tokens And Removes Refresh Cookie")
        void logout_WhenCalled_ClearsTokensAndRemovesRefreshCookie() {
            // TODO
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Unauthorized Request Returns 401")
        void accessProtectedEndpoint_WithoutAuth_Returns401() {
            // TODO
        }

        @Test
        @DisplayName("Expired Token Returns 401")
        void accessProtectedEndpoint_WithExpiredToken_Returns401() {
            // TODO
        }

        @Test
        @DisplayName("Invalid Token Returns 401")
        void accessProtectedEndpoint_WithInvalidToken_Returns401() {
            // TODO
        }
    }

    @Nested
    @DisplayName("Routing Tests")
    class RoutingTests {

        @Test
        @DisplayName("Authenticated Request To Patients Service Is Routed Properly")
        void authenticatedRequest_ToPatientsService_IsRoutedProperly() {
            // TODO
        }

        @Test
        @DisplayName("Authenticated Request To Notes Service Is Routed Properly")
        void authenticatedRequest_ToNotesService_IsRoutedProperly() {
            // TODO
        }

        @Test
        @DisplayName("Authenticated Request To Assessments Service Is Routed Properly")
        void authenticatedRequest_ToAssessmentsService_IsRoutedProperly() {
            // TODO
        }

        @Test
        @DisplayName("Public Endpoints Are Accessible Without Authentication")
        void publicEndpoints_WithoutAuthentication_AreAccessible() {
            // TODO
        }
    }
}
