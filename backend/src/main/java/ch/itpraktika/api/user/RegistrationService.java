package ch.itpraktika.api.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Das Passwort darf maximal 72 UTF-8-Bytes enthalten."
            );
        }
        if (userRepository.existsByEmail(email)) {
            throw emailAlreadyRegistered();
        }

        User student = new User(
                UUID.randomUUID().toString(),
                email,
                passwordEncoder.encode(request.password()),
                "student"
        );
        try {
            userRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException exception) {
            throw emailAlreadyRegistered();
        }

        return new RegistrationResponse(email, "student");
    }

    private ResponseStatusException emailAlreadyRegistered() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Diese E-Mail-Adresse ist bereits registriert."
        );
    }
}
