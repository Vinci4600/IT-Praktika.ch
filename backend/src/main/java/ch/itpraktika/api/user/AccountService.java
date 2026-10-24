package ch.itpraktika.api.user;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRevocationService tokenRevocationService;

    public AccountService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenRevocationService tokenRevocationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Transactional
    public void deleteAccount(String email, String password, JwtAuthenticationToken authentication) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(BAD_REQUEST, "Das Passwort darf maximal 72 UTF-8-Bytes enthalten.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Das Passwort ist ungültig."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Das Passwort ist ungültig.");
        }
        if ("admin".equals(user.getRole()) && userRepository.countByRole("admin") <= 1) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Das letzte Admin-Konto kann nicht gelöscht werden."
            );
        }

        tokenRevocationService.revoke(authentication);
        userRepository.delete(user);
    }
}
