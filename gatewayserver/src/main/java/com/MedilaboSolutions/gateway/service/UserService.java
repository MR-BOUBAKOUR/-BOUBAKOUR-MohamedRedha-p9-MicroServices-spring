package com.MedilaboSolutions.gateway.service;

import com.MedilaboSolutions.gateway.model.User;
import com.MedilaboSolutions.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // The @Transactional annotation is not required nor fully supported in reactive R2DBC context.
    // For multistep transactional logic, manual transaction management is needed.
    // For simple CRUD operations, this setup is sufficient and recommended for reactive applications.

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found.")));
    }

    public Mono<User> updateUserPicture(User user, String pictureUrl) {
        user.setUrlPicture(pictureUrl);
        return userRepository.save(user);
    }
}
