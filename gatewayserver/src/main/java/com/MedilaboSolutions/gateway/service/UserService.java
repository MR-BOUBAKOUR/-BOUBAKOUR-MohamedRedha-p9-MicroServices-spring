package com.MedilaboSolutions.gateway.service;

import com.MedilaboSolutions.gateway.model.User;
import com.MedilaboSolutions.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateUserPicture(User user, String pictureUrl) {
        user.setUrlPicture(pictureUrl);
        userRepository.save(user);
    }

    public Mono<User> findByUsernameReactive(String username) {
        return Mono
                .fromCallable(() -> userRepository.findByUsername(username))
                .subscribeOn(Schedulers.boundedElastic())
                .map(userOpt -> userOpt.orElseThrow(() -> new RuntimeException("User not found")));
    }

    public Mono<Void> updateUserPictureReactive(User user, String pictureUrl) {
        return Mono
                .fromRunnable(() -> {
                    user.setUrlPicture(pictureUrl);
                    userRepository.save(user);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
