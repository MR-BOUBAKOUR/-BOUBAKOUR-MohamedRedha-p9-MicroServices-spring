package com.MedilaboSolutions.gateway.security;

import com.MedilaboSolutions.gateway.filters.AuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthFilter authFilter;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS configuration to allow requests from the Vue.js frontend running on localhost during development
                // This setup enables cross-origin requests with credentials (headers)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
                    return config;
                }))
                // When an unauthenticated user tries to access a protected resource,
                // delegate the response to the UnauthorizedEntryPoint to return a 401 Unauthorized status.
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedEntryPoint))
                // Allow healthcheck for Docker; avoid exposing actuator endpoints in prod
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/login",
                                "/logout",
                                "/refresh",
                                "/actuator/**"
                        ).permitAll()
                        .anyExchange().hasRole("MEDECIN")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // Add custom logout handler to clear the cookie
                        .logoutHandler(cookieClearingLogoutHandler)
                        // Configure logout success to return 200 OK
                        .logoutSuccessHandler((webFilterExchange, authentication) ->
                                webFilterExchange.getExchange().getResponse().setComplete()
                        )
                )
                // Adds the custom JWT auth filter BEFORE Spring Security’s default authentication processing
                .addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                // Disable HTTP Basic auth (no browser popup)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // Disable form login (no login page)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User
                .withUsername("medecin")
                .password(encoder.encode("123"))
                .roles("MEDECIN")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    ServerLogoutHandler cookieClearingLogoutHandler = (webFilterExchange, authentication) -> {
        // Create a cookie with the "same name" but with an empty value to delete it
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)              // ⚠️ In production, need to be true (HTTPS)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)      // Set the cookie's max age to zero to expire it immediately
                .path("/")
                .build();

        webFilterExchange.getExchange().getResponse().addCookie(clearCookie);
        return Mono.empty();                // Indicate that the handler has completed its work
    };

}
