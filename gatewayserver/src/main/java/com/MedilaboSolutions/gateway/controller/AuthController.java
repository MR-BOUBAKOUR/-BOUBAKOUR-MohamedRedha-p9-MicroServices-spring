package com.MedilaboSolutions.gateway.controller;

import com.MedilaboSolutions.gateway.dto.AuthRequest;
import com.MedilaboSolutions.gateway.dto.AuthResponse;
import com.MedilaboSolutions.gateway.service.UserService;
import com.MedilaboSolutions.gateway.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final UserService userService;
    private final AuthUtil authUtil;
    private final PasswordEncoder encoder;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest,
                                                    ServerHttpResponse response) {
        log.info("login start");

        return reactiveUserDetailsService
            .findByUsername(authRequest.getUsername())
            .filter(user -> encoder.matches(authRequest.getPassword(), user.getPassword()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
            .flatMap(userDetails -> userService.findByUsername(userDetails.getUsername()))
            .flatMap(user -> {
                String username = user.getUsername();
                String role = "ROLE_" + user.getRole();
                String pictureUrl = user.getUrlPicture();

                // Generate access and refresh tokens
                String accessToken = authUtil.generateAccessToken(username, role, null);
                String refreshToken = authUtil.generateRefreshToken(username);

                // Store refresh token in an HttpOnly cookie which will be sent only in requests from the same domain
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)         // Prevent access from JavaScript (XSS protection)
                    .secure(true)          // ⚠️ In production, need to be true (HTTPS)
                    .sameSite("Strict")     // Cross-site requests won’t include the cookie, (CSRF attacks protection)
                    .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                    .path("/")              // Cookie included in requests to all paths on the same domain
                    .build();

                response.addCookie(refreshCookie);

                // Send access token in the response body
                AuthResponse loginResponse = new AuthResponse(
                        accessToken,
                        accessTokenExpirationMs,
                        user.getEmail(),
                        user.getUsername(),
                        pictureUrl
                );

                log.info("Login success for user: {}", username);
                return Mono.just(ResponseEntity.ok(loginResponse));
            })
            .doOnError(e -> log.warn("Login failed for user {}: {}", authRequest.getUsername(), e.getMessage()))
            .onErrorResume(e -> {
                if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                }

                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refresh(ServerWebExchange exchange) {
        log.info("refresh start");

        return Mono
                // Get refresh token from the cookie, validate it, then extract the username
                .fromCallable(() -> {
                    MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
                    List<HttpCookie> refreshCookies = cookies.get("refreshToken");

                    if (refreshCookies == null || refreshCookies.isEmpty()) {
                        log.warn("Refresh token missing");
                        throw new BadCredentialsException("Refresh token missing");
                    }

                    String refreshToken = refreshCookies.getFirst().getValue();

                    if (!authUtil.isValidToken(refreshToken) || !authUtil.isValidRefreshTokenType(refreshToken)) {
                        log.warn("Invalid refresh token");
                        throw new BadCredentialsException("Invalid refresh token");
                    }

                    log.info("refresh success");

                    return authUtil.getAllClaimsFromToken(refreshToken).getSubject();
                })
                .flatMap(username ->
                        reactiveUserDetailsService.findByUsername(username)
                                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found: " + username)))
                                // UserDetails found, now retrieve full User entity
                                .flatMap(userDetails -> userService.findByUsername(userDetails.getUsername()))
                )
                .map(user -> {
                    String role = "ROLE_" + user.getRole();
                    String username = user.getUsername();
                    String pictureUrl = user.getUrlPicture();
                    String newAccessToken = authUtil.generateAccessToken(user.getUsername(), role, pictureUrl);

                    log.info("Refresh token success for user: {}", username);

                    // Send access token in the response body
                    AuthResponse refreshResponse = new AuthResponse(
                            newAccessToken,
                            accessTokenExpirationMs,
                            user.getEmail(),
                            user.getUsername(),
                            pictureUrl
                    );
                    return ResponseEntity.ok(refreshResponse);
                })
                .onErrorResume(e -> {
                    log.warn("Refresh token failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }
}
