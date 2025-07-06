package com.MedilaboSolutions.gateway.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresContainerTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("gateway_users")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("users.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String host = POSTGRES_CONTAINER.getHost();
        int port = POSTGRES_CONTAINER.getMappedPort(5432);
        String dbName = POSTGRES_CONTAINER.getDatabaseName();
        String r2dbcUrl = "r2dbc:postgresql://" + host + ":" + port + "/" + dbName;

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES_CONTAINER::getPassword);

        registry.add("jwt.secretKey", () -> "test-secret-key-minimum-256-bits-for-hs256-algorithm");
        registry.add("jwt.accessTokenExpirationMs", () -> 10_000L);
        registry.add("jwt.refreshTokenExpirationMs", () -> 60_000L);
    }
}
