package ch.itpraktika.api.internship;

import ch.itpraktika.api.config.SecurityConfig;
import ch.itpraktika.api.user.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminInternshipController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class AdminInternshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternshipService internshipService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    private final UUID id = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListAllInternshipsForAdmin() throws Exception {
        when(internshipService.listAll()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/admin/stellen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("published"))
                .andExpect(jsonPath("$[0].titel").value("Praktikum Softwareentwicklung"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateInternship() throws Exception {
        when(internshipService.create(any(InternshipRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/admin/stellen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/admin/stellen/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateInternship() throws Exception {
        when(internshipService.update(eq(id), any(InternshipRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/v1/admin/stellen/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteInternship() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/stellen/{id}", id))
                .andExpect(status().isNoContent());

        verify(internshipService).delete(id);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldRejectStudent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stellen"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stellen"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/admin/stellen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titel":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRequireDateWhenStartOptionIsDate() throws Exception {
        String request = validRequest().replace("\"startDatum\": \"2026-11-01\"", "\"startDatum\": null");

        mockMvc.perform(post("/api/v1/admin/stellen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private String validRequest() {
        return """
                {
                  "firmaId": null,
                  "firma": "Beispiel AG",
                  "titel": "Praktikum Softwareentwicklung",
                  "standort": "Bern",
                  "dauer": "6 Monate",
                  "startOption": "DATE",
                  "startDatum": "2026-11-01",
                  "beruf": "Applikationsentwickler EFZ",
                  "ansprechperson": "Max Muster",
                  "email": "praktikum@example.com",
                  "telefon": "+41 31 000 00 00",
                  "bewerbungsUrl": null,
                  "entlohnung": "nach Vereinbarung",
                  "beschreibung": "Ein Praktikum in der Softwareentwicklung.",
                  "anforderungen": ["Interesse an Software"],
                  "status": "PUBLISHED"
                }
                """;
    }

    private InternshipResponse response() {
        return new InternshipResponse(
                id.toString(),
                null,
                "Praktikum Softwareentwicklung",
                "Beispiel AG",
                "Bern",
                "6 Monate",
                "date",
                LocalDate.of(2026, 11, 1),
                "Applikationsentwickler EFZ",
                "Max Muster",
                "praktikum@example.com",
                "+41 31 000 00 00",
                null,
                "nach Vereinbarung",
                "Ein Praktikum in der Softwareentwicklung.",
                List.of("Interesse an Software"),
                "published"
        );
    }
}
