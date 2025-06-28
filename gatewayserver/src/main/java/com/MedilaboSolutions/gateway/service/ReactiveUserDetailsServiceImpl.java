package com.MedilaboSolutions.gateway.service;

import com.MedilaboSolutions.gateway.model.User;
import com.MedilaboSolutions.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository
                .findByUsername(username))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(userOpt -> userOpt.map(this::toUserDetails)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new UsernameNotFoundException("User not found: " + username)))
                );
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .build();
    }
}