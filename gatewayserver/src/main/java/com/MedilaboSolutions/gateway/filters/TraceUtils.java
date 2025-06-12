package com.MedilaboSolutions.gateway.filters;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class TraceUtils {

    public static final String CORRELATION_ID_HEADER = "medilabo-solutions-correlation-id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        return requestHeaders.getFirst(CORRELATION_ID_HEADER);
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID_HEADER, correlationId);
    }

    // the object ServerWebExchange contain : the request + the response and the contexte of the HTTP call
    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String headerName, String headerValue) {
        // WebFlux objects are immutable, so we clone and update the request with a new header
        return exchange
                .mutate()
                .request(exchange
                        .getRequest()
                        .mutate()
                        .header(headerName, headerValue)
                        .build())
                .build();
    }

}
