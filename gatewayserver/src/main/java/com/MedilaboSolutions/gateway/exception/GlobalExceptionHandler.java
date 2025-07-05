package com.MedilaboSolutions.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<Void> handleBadCredentials(ServerWebExchange exchange, BadCredentialsException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @ExceptionHandler(Exception.class)
    public Mono<Void> handleGeneral(Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().setComplete();
    }

}
