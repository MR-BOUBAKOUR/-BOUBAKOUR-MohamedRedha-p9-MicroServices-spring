package com.MedilaboSolutions.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    // Simplifies frontend OAuth2 login by routing through backend endpoint.
    @GetMapping("/google")
    public Mono<ResponseEntity<Void>> redirectToGoogle() {
        return Mono.just(ResponseEntity
                .status(302)
                .header("Location", "/oauth2/authorization/google")
                .build());
    }
}
