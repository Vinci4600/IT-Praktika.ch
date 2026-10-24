package ch.itpraktika.api.company;

import ch.itpraktika.api.config.SecurityConfig;
import ch.itpraktika.api.user.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import({CompanyService.class, SecurityConfig.class})
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class CompanyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyRepository companyRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void shouldReturnEmptyCompanyList() throws Exception {
        when(companyRepository.findAllByOrderByNameAsc()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/firmen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCompany() throws Exception {
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/firmen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"],
                                  "contactPerson": "Lea Beispiel",
                                  "initiativeListed": true,
                                  "internshipUrl": "https://example.org/praktikum"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Beispiel AG"))
                .andExpect(jsonPath("$.standort").value("Bern"))
                .andExpect(jsonPath("$.spezialitaeten[0]").value("Softwareentwicklung"))
                .andExpect(jsonPath("$.contactPerson").value("Lea Beispiel"))
                .andExpect(jsonPath("$.initiativeListed").value(true))
                .andExpect(jsonPath("$.internshipUrl").value("https://example.org/praktikum"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCompany() throws Exception {
        UUID id = UUID.randomUUID();
        Company company = new Company(
                id.toString(),
                "Beispiel AG",
                "Bern",
                List.of("Softwareentwicklung"),
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null,
                null
        );
        when(companyRepository.findById(id.toString())).thenReturn(Optional.of(company));

        mockMvc.perform(delete("/api/v1/firmen/delete/{id}", id))
                .andExpect(status().isNoContent());

        verify(companyRepository).delete(company);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenDeletingUnknownCompany() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id.toString())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/firmen/delete/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCompany() throws Exception {
        UUID id = UUID.randomUUID();
        Company company = new Company(
                id.toString(),
                "Alte Firma",
                "Bern",
                List.of("Support"),
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null,
                null
        );
        when(companyRepository.findById(id.toString())).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/v1/firmen/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Neue Firma",
                                  "location": "Zürich",
                                  "specialties": ["Softwareentwicklung"],
                                  "partner": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Neue Firma"))
                .andExpect(jsonPath("$.standort").value("Zürich"))
                .andExpect(jsonPath("$.partner").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnNotFoundWhenUpdatingUnknownCompany() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id.toString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/firmen/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Neue Firma",
                                  "location": "Zürich",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCompanyWithoutName() throws Exception {
        mockMvc.perform(post("/api/v1/firmen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectCompanyCreationWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/firmen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(companyRepository);
    }

    @Test
    void shouldRejectCompanyCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/firmen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(companyRepository);
    }

    @Test
    void shouldAllowCompanyCreationWithAdminJwt() throws Exception {
        when(companyRepository.save(any(Company.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/firmen")
                        .header("Authorization", "Bearer " + createToken("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectCompanyCreationWithStudentJwt() throws Exception {
        mockMvc.perform(post("/api/v1/firmen")
                        .header("Authorization", "Bearer " + createToken("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(companyRepository);
    }

    @Test
    void shouldUseCurrentDatabaseRoleInsteadOfJwtRoleClaim() throws Exception {
        String token = createToken("ADMIN");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("")
                        .roles("STUDENT")
                        .build());

        mockMvc.perform(post("/api/v1/firmen")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Beispiel AG",
                                  "location": "Bern",
                                  "specialties": ["Softwareentwicklung"]
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(companyRepository);
    }

    @Test
    void shouldRejectTokenForDeletedAccount() throws Exception {
        String token = createToken("ADMIN");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenThrow(new UsernameNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/firmen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredJwt() throws Exception {
        mockMvc.perform(get("/api/v1/firmen")
                        .header("Authorization", "Bearer " + createToken("ADMIN", Instant.now().minusSeconds(120), Instant.now().minusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/firmen")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectJwtWithInvalidSignature() throws Exception {
        String[] tokenParts = createToken("ADMIN").split("\\.");
        char replacement = tokenParts[2].charAt(0) == 'A' ? 'B' : 'A';
        tokenParts[2] = replacement + tokenParts[2].substring(1);

        mockMvc.perform(get("/api/v1/firmen")
                        .header("Authorization", "Bearer " + String.join(".", tokenParts)))
                .andExpect(status().isUnauthorized());
    }

    private String createToken(String role) {
        Instant now = Instant.now();
        return createToken(role, now, now.plusSeconds(60));
    }

    private String createToken(String role, Instant issuedAt, Instant expiresAt) {
        lenient().when(revokedTokenRepository.existsByTokenIdAndExpiresAtAfter(any(), any()))
                .thenReturn(false);
        lenient().when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername("test@example.com")
                        .password("")
                        .roles(role)
                        .build());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ch.itpraktika.api.user.JwtTokenService.ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject("test@example.com")
                .claim("roles", List.of(role))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
    }
}
