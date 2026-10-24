package ch.itpraktika.api.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Validator validator,
            @Value("${app.security.bootstrap-admin.email:}") String email,
            @Value("${app.security.bootstrap-admin.password:}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            LOGGER.info("Admin bootstrap skipped because no bootstrap credentials were configured.");
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_EMAIL und ADMIN_BOOTSTRAP_PASSWORD müssen gemeinsam gesetzt sein."
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        BootstrapCredentials credentials = new BootstrapCredentials(normalizedEmail, password);
        Set<ConstraintViolation<BootstrapCredentials>> violations = validator.validate(credentials);
        if (!violations.isEmpty()
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Admin-Bootstrap ungültig: gültige E-Mail und Passwort mit mindestens 12 Zeichen "
                            + "(maximal 72 UTF-8-Bytes) erforderlich."
            );
        }

        User existingUser = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingUser != null && "admin".equals(existingUser.getRole())) {
            LOGGER.info("Admin bootstrap skipped because the configured admin already exists.");
            return;
        }
        if (existingUser != null) {
            throw new IllegalStateException(
                    "Die Bootstrap-E-Mail gehört bereits zu einem Benutzer ohne Admin-Rolle."
            );
        }

        User admin = new User(
                UUID.randomUUID().toString(),
                normalizedEmail,
                passwordEncoder.encode(password),
                "admin"
        );
        userRepository.save(admin);
        LOGGER.info("Initial admin account created.");
    }

    private record BootstrapCredentials(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12) String password
    ) {
    }
}
