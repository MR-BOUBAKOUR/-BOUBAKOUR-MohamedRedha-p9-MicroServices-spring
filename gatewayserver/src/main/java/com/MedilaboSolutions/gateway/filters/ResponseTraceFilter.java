package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.utils.TraceUtil;
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
    TraceUtil traceUtil;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) ->
            chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Copy correlation ID from the request headers to the response headers for end-to-end tracing
                    String correlationId = traceUtil.getCorrelationId(exchange.getRequest().getHeaders());

                    // log.info("Updated the correlation id to the response headers: {}", correlationId);
                    exchange.getResponse().getHeaders()
                        .add(TraceUtil.CORRELATION_ID_HEADER, correlationId);
        }));
    }
}