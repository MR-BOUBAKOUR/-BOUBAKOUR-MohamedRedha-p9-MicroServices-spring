package com.MedilaboSolutions.gateway.controller;

import com.MedilaboSolutions.gateway.dto.AuthRequest;
import com.MedilaboSolutions.gateway.dto.AuthResponse;
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

    private final ReactiveUserDetailsService userDetailsService;
    private final AuthUtil authUtil;
    private final PasswordEncoder encoder;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(
            @RequestBody AuthRequest authRequest,
            ServerHttpResponse response
    ) {
        return userDetailsService
            .findByUsername(authRequest.getUsername())
            .filter(user -> encoder.matches(authRequest.getPassword(), user.getPassword()))
            .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
            .map(user -> {
                String username = user.getUsername();
                String role = user.getAuthorities().iterator().next().getAuthority();

                log.info("Login success for user: {}", username);

                // Generate access and refresh tokens
                String accessToken = authUtil.generateAccessToken(username, role, null);
                String refreshToken = authUtil.generateRefreshToken(username);

                // Store refresh token in an HttpOnly cookie which will be sent only in requests from the same domain
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)         // Prevent access from JavaScript (XSS protection)
                    .secure(true)          // ⚠️ In production, need to be true (HTTPS)
                    .sameSite("None")     // Cross-site requests won’t include the cookie, (CSRF attacks protection)
                    .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                    .path("/")              // Cookie included in requests to all paths on the same domain
                    .build();

                response.addCookie(refreshCookie);

                // Send access token in the response body
                AuthResponse loginResponse = new AuthResponse(accessToken, accessTokenExpirationMs);
                return ResponseEntity.ok(loginResponse);
            })
            .doOnError(e -> log.warn("Login failed for user {}: {}", authRequest.getUsername(), e.getMessage()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refresh(ServerWebExchange exchange) {
        return Mono
                // Get refresh token from the cookie, validate it, then extract the username
                // .fromCallable: wraps a blocking or synchronous task into a Mono for reactive execution
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

                    return authUtil.getAllClaimsFromToken(refreshToken).getSubject();
                })
                // Retrieves the user, generates a new access token with their role, and returns it in the response.
                // .flatMap: transforms the Mono asynchronously by flattening nested Monos into a single stream
                .flatMap(username ->
                    userDetailsService
                            .findByUsername(username)
                            .switchIfEmpty(Mono.error(new BadCredentialsException("User not found: " + username)))
                            .map(user -> {
                                String role = user.getAuthorities().iterator().next().getAuthority();

                                log.info("Refresh token success for user: {}", username);

                                // Generate a new access tokens
                                String newAccessToken = authUtil.generateAccessToken(username, role, null);

                                AuthResponse refreshResponse = new AuthResponse(newAccessToken, accessTokenExpirationMs);
                                return ResponseEntity.ok(refreshResponse);
                            })
                )
                // .onErrorResume: handles errors by switching to a fallback Mono
                .onErrorResume(e -> {
                    log.warn("Refresh token failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }

//    @PostMapping("/signout")
//    public Mono<ResponseEntity<Void>> signout(ServerHttpResponse response) {
//        // Create a cookie with the same name but empty value to delete it
//        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
//                .httpOnly(true)
//                .secure(false)              // ⚠️ In production, need to be true (HTTPS)
//                .sameSite("Strict")
//                .maxAge(Duration.ZERO)      // Set the cookie's max age to zero to expire it immediately
//                .path("/")
//                .build();
//
//        response.addCookie(clearCookie);
//
//        log.info("User logged out");
//
//        // Signout is a simple immediate action: just delete the cookie
//        // No asynchronous processing or waiting needed
//        // Return a 200 OK with empty body to confirm success
//        // Mono.just(...) creates a Mono with an immediate value
//        return Mono.just(ResponseEntity.ok().build());
//    }
}
