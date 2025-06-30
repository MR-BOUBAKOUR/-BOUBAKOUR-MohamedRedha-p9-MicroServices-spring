package com.MedilaboSolutions.gateway.repository;

import com.MedilaboSolutions.gateway.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Mono<User> findByUsername(String username);
}
