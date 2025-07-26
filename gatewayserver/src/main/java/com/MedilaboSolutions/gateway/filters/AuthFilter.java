package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.utils.AuthUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthFilter implements WebFilter {

    private final AuthUtil authUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Skip authentication for public paths
        String path = exchange.getRequest().getURI().getPath();
        if (
                path.startsWith("/login") ||
                path.startsWith("/login/oauth2/code/") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/refresh") ||
                path.startsWith("/logout") ||
                path.startsWith("/eureka") ||
                path.startsWith("/actuator")
        ) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            Claims claims;
            try {
                claims = authUtil.getCachedClaims(token);
            } catch (JwtException e) {
                log.warn("Token rejected: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        } else {
            log.warn("Missing token");
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}