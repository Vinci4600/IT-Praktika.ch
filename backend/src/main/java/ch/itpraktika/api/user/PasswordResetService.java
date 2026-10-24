package ch.itpraktika.api.user;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_LIFETIME_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromAddress;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository resetTokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public void requestReset(String requestedEmail) {
        String email = requestedEmail.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(email).ifPresent(user -> {
            resetTokenRepository.deleteByUser_Id(user.getId());
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            Instant expiresAt = Instant.now().plus(TOKEN_LIFETIME_MINUTES, ChronoUnit.MINUTES);
            resetTokenRepository.save(new PasswordResetToken(user, hash(token), expiresAt));

            try {
                sendResetEmail(user.getEmail(), token);
            } catch (MailException | MessagingException exception) {
                LOGGER.error(
                        "Passwort-Reset-E-Mail konnte nicht versendet werden ({}).",
                        exception.getClass().getSimpleName()
                );
            }
        });
    }

    @Transactional
    public void confirmReset(PasswordResetConfirmRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(BAD_REQUEST, "Das Passwort darf maximal 72 UTF-8-Bytes enthalten.");
        }
        PasswordResetToken token = resetTokenRepository.findByTokenHash(hash(request.token()))
                .filter(resetToken -> resetToken.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Der Passwort-Reset-Link ist ungültig oder abgelaufen."
                ));
        User user = token.getUser();
        user.changePassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        resetTokenRepository.delete(token);
    }

    private void sendResetEmail(String email, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
        helper.setFrom(fromAddress);
        helper.setTo(email);
        helper.setSubject("Passwort für IT-Praktika zurücksetzen");
        helper.setText("""
                Du hast angefordert, dein Passwort zurückzusetzen.

                Öffne innerhalb von 30 Minuten diesen Link:
                %s/login?resetToken=%s

                Wenn du diese Anfrage nicht gestellt hast, kannst du diese E-Mail ignorieren.
                """.formatted(frontendUrl, token));
        mailSender.send(message);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 ist in dieser Java-Laufzeit nicht verfügbar.", exception);
        }
    }
}
