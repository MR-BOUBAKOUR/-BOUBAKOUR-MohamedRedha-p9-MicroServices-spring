package com.MedilaboSolutions.gateway.config;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
public class NettyConfig {

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("custom")
                .maxConnections(300)
                .pendingAcquireMaxCount(100)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .maxIdleTime(Duration.ofSeconds(20))
                .evictInBackground(Duration.ofSeconds(30))
                .metrics(true)
                .build();
    }

    @Bean
    public HttpClient httpClient(ConnectionProvider connectionProvider) {

        int eventLoopThreads = Runtime.getRuntime().availableProcessors();

        return HttpClient.create(connectionProvider)
                .runOn(LoopResources.create("gateway", eventLoopThreads, true))
                .metrics(true, uri -> uri);
    }

}
