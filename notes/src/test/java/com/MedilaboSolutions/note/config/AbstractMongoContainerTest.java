package com.MedilaboSolutions.note.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public abstract class AbstractMongoContainerTest {

    @Container
    static final GenericContainer<?> MONGO_CONTAINER = new GenericContainer<>("mongo:6")
            .withExposedPorts(27017)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init-mongo.js"),
                    "/docker-entrypoint-initdb.d/init-mongo.js")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("notes.json"),
                    "/docker-entrypoint-initdb.d/notes.json");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String host = MONGO_CONTAINER.getHost();
        int port = MONGO_CONTAINER.getMappedPort(27017);
        String dbName = "medilabosolutions";
        String mongoUri = "mongodb://" + host + ":" + port + "/" + dbName;

        registry.add("spring.data.mongodb.uri", () -> mongoUri);
    }
}
