package com.MedilaboSolutions.gateway.filters;

import com.MedilaboSolutions.gateway.service.UserService;
import com.MedilaboSolutions.gateway.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Order(-2)
@Component
public class SseAuthFilter implements WebFilter {

    private final AuthUtil authUtil;
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Appliquer uniquement aux routes SSE
        if (path.startsWith("/v1/assessments/sse/")) {
            log.info("SseAuthWebFilter → processing SSE request: {}", path);

            List<HttpCookie> cookies = exchange.getRequest().getCookies().get("refreshToken");
            if (cookies == null || cookies.isEmpty()) {
                log.warn("SseAuthWebFilter → No refresh token cookie");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String refreshToken = cookies.getFirst().getValue();
            if (!authUtil.isValidToken(refreshToken) || !authUtil.isValidRefreshTokenType(refreshToken)) {
                log.warn("SseAuthWebFilter → Invalid refresh token");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String username = authUtil.getAllClaimsFromToken(refreshToken).getSubject();

            // Récupérer le rôle et la photo directement depuis la DB
            return userService.findByUsername(username)
                    .switchIfEmpty(Mono.error(new BadCredentialsException("User not found: " + username)))
                    .flatMap(user -> {
                        String role = "ROLE_" + user.getRole();
                        String accessToken = authUtil.generateAccessToken(user.getUsername(), role, user.getUrlPicture());

                        log.info("SseAuthWebFilter → user={} role={} accessToken injected", user.getUsername(), role);

                        return chain.filter(
                                exchange.mutate()
                                        .request(r -> r.headers(h -> h.setBearerAuth(accessToken)))
                                        .build()
                        );
                    })
                    .onErrorResume(e -> {
                        log.warn("SseAuthWebFilter → authentication failed: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }

        // Routes normales : passe la requête
        return chain.filter(exchange);
    }
}
