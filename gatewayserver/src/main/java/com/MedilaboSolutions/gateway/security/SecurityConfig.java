package com.MedilaboSolutions.gateway.security;

import com.MedilaboSolutions.gateway.filters.AuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthFilter authFilter;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    SecurityContextServerLogoutHandler securityContextLogoutHandler = new SecurityContextServerLogoutHandler();

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // CORS configuration to allow requests from the Vue.js frontend running on localhost during development
                // This setup enables cross-origin requests with credentials (headers)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.setAllowedOrigins(List.of("https://localhost:5173"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
                    return config;
                }))
                // When an unauthenticated user tries to access a protected resource,
                // delegate the response to the UnauthorizedEntryPoint to return a 401 Unauthorized status.
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedEntryPoint))
                // Allow healthcheck for Docker; avoid exposing actuator endpoints in prod
                .authorizeExchange(ex -> ex
                        // Only the user with ROLE_MEDECIN can access their own user info
                        // Prevents exposing sensitive user data (even if, there is none if no one is logged in)
                        .pathMatchers("/oauth2/user").hasRole("MEDECIN")
                        .pathMatchers(
                                "/login",
                                // OAuth2 callback URL where Spring Security receives the authorization code, exchanges
                                // it for tokens, and builds the authenticated user details with the user attributes
                                "/login/oauth2/code/**",
                                "/oauth2/**",
                                "/refresh",
                                "/logout",
                                "/actuator/**"
                        ).permitAll()
                        .anyExchange().hasRole("MEDECIN")
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(oauth2SuccessHandler)
                        .authenticationFailureHandler(oauth2FailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(securityContextLogoutHandler)
                        .logoutSuccessHandler((webFilterExchange, authentication) -> {

                            log.info("Logout detected, clearing refreshToken cookie for user: {}",
                                    (authentication != null) ? authentication.getName() : "anonymous");
                            ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                                    .httpOnly(true)
                                    .secure(true)
                                    .sameSite("Strict")
                                    .maxAge(Duration.ZERO)
                                    .path("/")
                                    .build();
                            webFilterExchange.getExchange().getResponse().addCookie(clearCookie);

                            return webFilterExchange.getExchange().getResponse().setComplete();
                        })
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
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return userDetailsServiceImpl;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
