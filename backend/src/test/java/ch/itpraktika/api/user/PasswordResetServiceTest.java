package ch.itpraktika.api.user;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                resetTokenRepository,
                passwordEncoder,
                mailSender,
                "https://it-praktika.example",
                "no-reply@example.test"
        );
    }

    @Test
    void shouldSendRandomResetLinkAndPersistOnlyItsHash() throws Exception {
        User user = new User(UUID.randomUUID().toString(), "student@example.com", "old-hash", "student");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        passwordResetService.requestReset(" STUDENT@example.com ");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenRepository).save(tokenCaptor.capture());
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        Matcher tokenMatcher = Pattern.compile("resetToken=([A-Za-z0-9_-]+)")
                .matcher(messageCaptor.getValue().getContent().toString());
        assertThat(tokenMatcher.find()).isTrue();
        assertThat(hash(tokenMatcher.group(1))).isEqualTo(saved.getTokenHash());
    }

    @Test
    void shouldNotSendEmailWhenAccountDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("missing@example.com");

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void shouldChangePasswordAndConsumeResetToken() throws Exception {
        String rawToken = "single-use-reset-token";
        User user = new User(UUID.randomUUID().toString(), "student@example.com", "old-hash", "student");
        PasswordResetToken token = new PasswordResetToken(
                user,
                hash(rawToken),
                Instant.now().plusSeconds(60)
        );
        when(resetTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-correct-horse")).thenReturn("new-hash");

        passwordResetService.confirmReset(new PasswordResetConfirmRequest(rawToken, "new-correct-horse"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
        verify(resetTokenRepository).delete(token);
    }

    @Test
    void shouldRejectExpiredOrUnknownResetToken() throws Exception {
        String rawToken = "expired-reset-token";
        PasswordResetToken token = new PasswordResetToken(
                new User(UUID.randomUUID().toString(), "student@example.com", "hash", "student"),
                hash(rawToken),
                Instant.now().minusSeconds(1)
        );
        when(resetTokenRepository.findByTokenHash(hash(rawToken))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.confirmReset(
                new PasswordResetConfirmRequest(rawToken, "new-correct-horse")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ungültig oder abgelaufen");
    }

    private static String hash(String value) throws Exception {
        return java.util.HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))
        );
    }
}
