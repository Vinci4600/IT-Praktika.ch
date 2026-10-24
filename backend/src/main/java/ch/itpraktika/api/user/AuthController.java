package ch.itpraktika.api.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RegistrationService registrationService;
    private final PasswordResetService passwordResetService;
    private final TokenRevocationService tokenRevocationService;
    private final AuthenticationRateLimitService rateLimitService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            RegistrationService registrationService,
            PasswordResetService passwordResetService,
            TokenRevocationService tokenRevocationService,
            AuthenticationRateLimitService rateLimitService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.registrationService = registrationService;
        this.passwordResetService = passwordResetService;
        this.tokenRevocationService = tokenRevocationService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String clientAddress = httpRequest.getRemoteAddr();
        rateLimitService.check("login", clientAddress, email, 10, Duration.ofMinutes(15));
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Das Passwort darf maximal 72 UTF-8-Bytes enthalten."
            );
        }
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email().trim().toLowerCase(Locale.ROOT),
                        request.password()
                )
        );
        String accessToken = jwtTokenService.createToken(authentication);
        rateLimitService.clear("login", clientAddress, email);
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_STUDENT"))
                .findFirst()
                .map(authority -> authority.substring("ROLE_".length()).toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalStateException("Der Benutzer hat keine gültige Rolle."));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new LoginResponse(
                        accessToken,
                        "Bearer",
                        JwtTokenService.TOKEN_LIFETIME_SECONDS,
                        authentication.getName(),
                        role
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitService.check(
                "register",
                httpRequest.getRemoteAddr(),
                "",
                5,
                Duration.ofHours(1)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.register(request));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitService.check(
                "password-reset",
                httpRequest.getRemoteAddr(),
                request.email().trim().toLowerCase(Locale.ROOT),
                3,
                Duration.ofHours(1)
        );
        passwordResetService.requestReset(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request,
            HttpServletRequest httpRequest
    ) {
        rateLimitService.check(
                "password-reset-confirm",
                httpRequest.getRemoteAddr(),
                "",
                10,
                Duration.ofHours(1)
        );
        passwordResetService.confirmReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(JwtAuthenticationToken authentication) {
        tokenRevocationService.revoke(authentication);
        return ResponseEntity.noContent().build();
    }
}
