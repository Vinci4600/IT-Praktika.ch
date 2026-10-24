package ch.itpraktika.api.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ch.itpraktika.api.config.SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private TokenRevocationService tokenRevocationService;

    @MockitoBean
    private AuthenticationRateLimitService authenticationRateLimitService;

    @Test
    void shouldIssueBearerTokenForValidLoginCredentials() throws Exception {
        Authentication authenticatedUser = UsernamePasswordAuthenticationToken.authenticated(
                "admin@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedUser);
        when(jwtTokenService.createToken(authenticatedUser)).thenReturn("signed.jwt.token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "correct-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(JwtTokenService.TOKEN_LIFETIME_SECONDS))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("admin"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenService).createToken(authenticatedUser);
    }

    @Test
    void shouldRejectInvalidLoginCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "some-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectPasswordAboveBcryptByteLimit() throws Exception {
        String password = "é".repeat(37);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "admin@example.com",
                                  "password": "%s"
                                }
                                """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REQUEST_ERROR"));
    }

    @Test
    void shouldRegisterStudentAccount() throws Exception {
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResponse("student@example.com", "student"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "correct-horse-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.role").value("student"));

        verify(registrationService).register(any(RegistrationRequest.class));
    }

    @Test
    void shouldRejectRegistrationWithShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectDuplicateRegistrationEmail() throws Exception {
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "Diese E-Mail-Adresse ist bereits registriert."
                ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "student@example.com",
                                  "password": "correct-horse-123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    @Test
    void shouldAlwaysAcceptPasswordResetRequestWithoutRevealingAccountPresence() throws Exception {
        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType("application/json")
                        .content("""
                                {"email":"student@example.com"}
                                """))
                .andExpect(status().isAccepted());

        verify(passwordResetService).requestReset("student@example.com");
    }

    @Test
    void shouldConfirmPasswordReset() throws Exception {
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType("application/json")
                        .content("""
                                {"token":"reset-token","password":"correct-horse-battery"}
                                """))
                .andExpect(status().isNoContent());

        verify(passwordResetService).confirmReset(any(PasswordResetConfirmRequest.class));
    }

    @Test
    void shouldRevokeAuthenticatedTokenOnLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isNoContent());

        verify(tokenRevocationService).revoke(any(JwtAuthenticationToken.class));
    }
}
