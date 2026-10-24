package ch.itpraktika.api.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RegistrationService registrationService =
            new RegistrationService(userRepository, passwordEncoder);

    @Test
    void shouldNormalizeEmailHashPasswordAndAlwaysCreateStudentRole() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("correct-horse-123")).thenReturn("hashed-password");

        RegistrationResponse response = registrationService.register(
                new RegistrationRequest(" Student@Example.com ", "correct-horse-123")
        );

        assertEquals("student@example.com", response.email());
        assertEquals("student", response.role());
        verify(passwordEncoder).encode("correct-horse-123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertEquals("student@example.com", userCaptor.getValue().getEmail());
        assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
        assertEquals("student", userCaptor.getValue().getRole());
    }

    @Test
    void shouldRejectAnExistingEmail() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> registrationService.register(
                        new RegistrationRequest("student@example.com", "correct-horse-123")
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void shouldRejectPasswordOverBcryptByteLimit() {
        String password = "é".repeat(37);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> registrationService.register(
                        new RegistrationRequest("student@example.com", password)
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, org.mockito.Mockito.never()).saveAndFlush(any(User.class));
    }
}
