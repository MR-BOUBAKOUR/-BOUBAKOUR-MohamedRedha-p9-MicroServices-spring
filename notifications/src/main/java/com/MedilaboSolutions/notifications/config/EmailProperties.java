package com.MedilaboSolutions.notifications.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    private String sender;
    private String recipient;
}
