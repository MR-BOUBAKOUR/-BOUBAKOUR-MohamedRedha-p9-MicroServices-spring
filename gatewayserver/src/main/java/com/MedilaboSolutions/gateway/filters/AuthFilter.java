package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.utils.AuthUtil;
import io.jsonwebtoken.Claims;
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

        log.debug("AuthFilter called for request id={}, method={}, path={}, headers={}",
                exchange.getRequest().getId(),
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getHeaders());

        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for public paths
        if (path.startsWith("/eureka") ||
                path.startsWith("/actuator") ||
                path.equals("/login") ||
                path.equals("/refresh") ||
                path.equals("/logout") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/code/")
        ) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            log.debug("Un token Bearer est présent. Tentative de validation JWT.");
            token = token.substring(7);

            if (authUtil.isValidToken(token)) {
                Claims claims = authUtil.getAllClaimsFromToken(token);
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
                log.warn("Invalid or expired token");
            }
        } else {
            log.warn("Missing token");
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}