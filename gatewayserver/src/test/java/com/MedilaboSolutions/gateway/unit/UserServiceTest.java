package com.MedilaboSolutions.gateway.unit;

import com.MedilaboSolutions.gateway.model.User;
import com.MedilaboSolutions.gateway.repository.UserRepository;
import com.MedilaboSolutions.gateway.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("Should return user when findByUsername succeeds")
    void shouldReturnUser_whenFindByUsername() {
        User user = new User(
                2L,
                "prenom_test",
                "email_test2@gmail.com",
                "password_test",
                null,
                null,
                "MEDECIN"
        );
        when(userRepository.findByUsername("prenom_test")).thenReturn(Mono.just(user));

        StepVerifier.create(userService.findByUsername("prenom_test"))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should emit UsernameNotFoundException when findByUsername returns empty")
    void shouldError_whenFindByUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(userService.findByUsername("unknown"))
                .expectErrorSatisfies(ex -> {
                    assert(ex instanceof UsernameNotFoundException);
                    assert(ex.getMessage().equals("User not found."));
                })
                .verify();
    }

    @Test
    @DisplayName("Should update user picture")
    void shouldUpdateUserPicture() {
        User user = new User(
                1L,
                "prenom_test",
                "email_test@gmail.com",
                "password_test",
                null,
                null,
                "MEDECIN"
        );
        User updatedUser = new User(
                1L,
                "prenom_test",
                "email_test@gmail.com",
                "password_test",
                "newPictureUrl",
                null,
                "MEDECIN"
        );

        when(userRepository.save(user)).thenReturn(Mono.just(updatedUser));

        StepVerifier.create(userService.updateUserPicture(user, "newPictureUrl"))
                .expectNextMatches(u -> u.getUrlPicture().equals("newPictureUrl"))
                .verifyComplete();
    }

}
