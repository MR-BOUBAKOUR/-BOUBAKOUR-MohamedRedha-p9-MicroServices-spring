package com.MedilaboSolutions.gateway.unit;

import com.MedilaboSolutions.gateway.model.User;
import com.MedilaboSolutions.gateway.repository.UserRepository;
import com.MedilaboSolutions.gateway.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return UserDetails when user exists")
    void shouldReturnUserDetails_whenUserExists() {
        User user = new User(
                1L,
                "prenom_test",
                "email_test@gmail.com",
                "password_test",
                null,
                null,
                "MEDECIN"
        );
        when(userRepository.findByUsername("prenom_test")).thenReturn(Mono.just(user));

        StepVerifier.create(userDetailsService.findByUsername("prenom_test"))
                .assertNext(userDetails -> {
                    assertThat(userDetails.getUsername()).isEqualTo("prenom_test");
                    assertThat(userDetails.getPassword()).isEqualTo("password_test");
                    assertThat(userDetails.getAuthorities())
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_MEDECIN"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should emit UsernameNotFoundException when user does not exist")
    void shouldError_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(userDetailsService.findByUsername("unknown"))
                .expectErrorMatches(error ->
                        error instanceof UsernameNotFoundException &&
                        error.getMessage().contains("unknown")
                )
                .verify();
    }
}
