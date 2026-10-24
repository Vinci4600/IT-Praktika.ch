package ch.itpraktika.api.user;

import ch.itpraktika.api.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class AccountControllerTest {

    private static final String EMAIL = "student@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void shouldDeleteOwnAccountAfterPasswordConfirmation() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(jwt().jwt(jwt -> jwt.subject(EMAIL))
                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"correct-horse-battery"}
                                """))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(any(), any(), any());
    }

    @Test
    void shouldRejectUnauthenticatedDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"correct-horse-battery"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
