package com.MedilaboSolutions.e2e_tests.config;

public class TestConfig {

    // URLs
    public static final String GATEWAY_BASE_URL = "https://localhost:8071";
    public static final String PATIENTS_API = GATEWAY_BASE_URL + "/v1/patients";
    public static final String NOTES_API = GATEWAY_BASE_URL + "/v1/notes";
    public static final String ASSESSMENTS_API = GATEWAY_BASE_URL + "/v1/assessments";
    public static final String LOGIN_API = GATEWAY_BASE_URL + "/login";
    public static final String REFRESH_API = GATEWAY_BASE_URL + "/refresh";
    public static final String LOGOUT_API = GATEWAY_BASE_URL + "/logout";
    public static final String HEALTH_API = GATEWAY_BASE_URL + "/actuator/health";

    public static final int DEFAULT_TIMEOUT = 60_000; // 30 sec

}

