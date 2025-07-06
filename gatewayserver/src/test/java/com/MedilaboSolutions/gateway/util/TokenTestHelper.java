package com.MedilaboSolutions.gateway.util;

import com.MedilaboSolutions.gateway.utils.AuthUtil;
import org.springframework.test.util.ReflectionTestUtils;

public class TokenTestHelper {

    // Utility class for generating test tokens (access and refresh) with predefined configurations
    // Used in integration or E2E tests only
    // Tokens generated here use a fake secret key and short expiration times, and are suitable only for testing

    private static AuthUtil createAuthUtil() {
        AuthUtil authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "test-secret-key-minimum-256-bits-for-hs256-algorithm");
        ReflectionTestUtils.setField(authUtil, "accessTokenExpirationMs", 10_000L);
        ReflectionTestUtils.setField(authUtil, "refreshTokenExpirationMs", 60_000L);
        return authUtil;
    }

    private static AuthUtil createExpiredAuthUtil() {
        AuthUtil authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "test-secret-key-minimum-256-bits-for-hs256-algorithm");
        ReflectionTestUtils.setField(authUtil, "accessTokenExpirationMs", 1L);
        ReflectionTestUtils.setField(authUtil, "refreshTokenExpirationMs", 1L);
        return authUtil;
    }

    public static String validAccessToken() {
        return createAuthUtil().generateAccessToken("username_test", "ROLE_MEDECIN", null);
    }

    public static String validAccessTokenWithImage() {
        return createAuthUtil().generateAccessToken("username_test", "ROLE_MEDECIN", "https://example.com/image.jpg");
    }

    public static String expiredAccessToken() {
        return createExpiredAuthUtil().generateAccessToken("username_test", "ROLE_MEDECIN", null);
    }

    public static String validRefreshToken() {
        return createAuthUtil().generateRefreshToken("username_test");
    }

    public static String expiredRefreshToken() {
        return createExpiredAuthUtil().generateRefreshToken("username_test");
    }

    public static String invalidToken() {
        return "invalid.token";
    }

    public static AuthUtil standardAuthUtil() {
        return createAuthUtil();
    }

    public static AuthUtil expiredTokenAuthUtil() {
        return createExpiredAuthUtil();
    }
}
