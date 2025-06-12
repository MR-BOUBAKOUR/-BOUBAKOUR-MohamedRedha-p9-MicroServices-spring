package com.MedilaboSolutions.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class ResponseTraceFilter {

    @Autowired
    TraceUtils traceUtils;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) ->
            chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Copy correlation ID from the request headers to the response headers for end-to-end tracing
                    String correlationId = traceUtils.getCorrelationId(exchange.getRequest().getHeaders());

                    log.debug("Updated the correlation id to the response headers: {}", correlationId);
                    exchange.getResponse().getHeaders()
                        .add(TraceUtils.CORRELATION_ID_HEADER, correlationId);
        }));
    }
}