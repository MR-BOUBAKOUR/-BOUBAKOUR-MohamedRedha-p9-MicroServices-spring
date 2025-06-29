package com.MedilaboSolutions.gateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static java.net.URLEncoder.encode;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements ServerAuthenticationFailureHandler {

    @Value("${cors.frontend-error-url}")
    private String frontendErrorUrl;

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        log.error("OAuth2 authentication failed", exception);
        String errorMessage = exception.getMessage();

        String redirectUrl = String.format(
                "%s?error=%s",
                frontendErrorUrl,
                encode(errorMessage, StandardCharsets.UTF_8)
        );

        ServerWebExchange exchange = webFilterExchange.getExchange();
        exchange.getResponse()
                .setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders()
                .setLocation(URI.create(redirectUrl));

        return exchange.getResponse().setComplete();
    }
}
