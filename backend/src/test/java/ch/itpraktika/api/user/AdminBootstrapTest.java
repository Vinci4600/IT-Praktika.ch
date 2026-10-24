package ch.itpraktika.api.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "a-long-safe-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldCreateAdminWithEncodedPasswordWhenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrap(EMAIL, PASSWORD).run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User createdUser = userCaptor.getValue();

        assertThat(createdUser.getId()).isNotBlank();
        assertThat(createdUser.getEmail()).isEqualTo(EMAIL);
        assertThat(createdUser.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(createdUser.getRole()).isEqualTo("admin");
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void shouldNotOverwriteExistingAdmin() {
        User existingAdmin = new User("existing-id", EMAIL, "existing-hash", "admin");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingAdmin));

        bootstrap(EMAIL, PASSWORD).run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldRejectPartialBootstrapConfiguration() {
        assertThrows(
                IllegalStateException.class,
                () -> bootstrap(EMAIL, "").run(null)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldSkipBootstrapWhenCredentialsAreNotConfigured() {
        bootstrap("", "").run(null);

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectPasswordShorterThanTwelveCharacters() {
        assertThrows(
                IllegalStateException.class,
                () -> bootstrap(EMAIL, "short").run(null)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    private AdminBootstrap bootstrap(String email, String password) {
        return new AdminBootstrap(userRepository, passwordEncoder, validator, email, password);
    }
}
