package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.utils.AuthUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for system endpoints such as Eureka registration, health checks, and login.
        // These endpoints must remain publicly accessible without requiring a JWT token.
        if (path.startsWith("/eureka") || path.startsWith("/actuator") || path.equals("/login")) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (authUtil.validateToken(token)) {
                Claims claims = authUtil.getAllClaimsFromToken(token);
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(authority)
                );

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            } else {
                log.warn("Token invalid or expired");
            }
        } else {
            log.warn("Token missing");
        }
        return chain.filter(exchange);
    }
}
