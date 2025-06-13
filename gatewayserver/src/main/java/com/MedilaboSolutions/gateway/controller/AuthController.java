package com.MedilaboSolutions.gateway.controller;

import com.MedilaboSolutions.gateway.dto.LoginRequest;
import com.MedilaboSolutions.gateway.dto.LoginResponse;
import com.MedilaboSolutions.gateway.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveUserDetailsService userDetailsService;
    private final AuthUtil authUtil;
    private final PasswordEncoder encoder;

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return userDetailsService
                .findByUsername(loginRequest.getUsername())
                .filter(user -> encoder.matches(loginRequest.getPassword(), user.getPassword()))
                .switchIfEmpty(
                        Mono.error(new BadCredentialsException("Invalid credentials"))
                )
                .map(user -> new LoginResponse(
                        authUtil.generateToken(
                                user.getUsername(),
                                user.getAuthorities().iterator().next().getAuthority()
                        )
                ));
    }
}
