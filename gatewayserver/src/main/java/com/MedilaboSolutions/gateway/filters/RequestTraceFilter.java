package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.utils.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Execute early in the filter chain
@Order(1)
@Slf4j
@RequiredArgsConstructor
@Component
public class RequestTraceFilter implements GlobalFilter {

    private final TraceUtil traceUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        String correlationId = traceUtil.getCorrelationId(requestHeaders);

        if (correlationId != null) {
            log.debug("medilabo-solutions-correlation-id found in RequestTraceFilter: {}", correlationId);
        } else {
            correlationId = generateCorrelationId();
            exchange = traceUtil.setCorrelationId(exchange, correlationId);
            log.debug("medilabo-solutions-correlation-id generated in RequestTraceFilter: {}", correlationId);
        }
        return chain.filter(exchange);
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

}
