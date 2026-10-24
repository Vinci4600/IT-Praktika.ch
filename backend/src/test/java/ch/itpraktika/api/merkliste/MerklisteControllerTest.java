package ch.itpraktika.api.merkliste;

import ch.itpraktika.api.config.SecurityConfig;
import ch.itpraktika.api.user.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(MerklisteController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class MerklisteControllerTest {

    private static final String EMAIL = "student@example.com";
    private static final UUID ENTRY_ID = UUID.randomUUID();
    private static final UUID INTERNSHIP_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerklisteService merklisteService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void shouldListCurrentStudentsEntries() throws Exception {
        when(merklisteService.getMerkliste(EMAIL)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/me/merkliste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ENTRY_ID.toString()))
                .andExpect(jsonPath("$[0].stelleId").value(INTERNSHIP_ID.toString()));

        verify(merklisteService).getMerkliste(EMAIL);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void shouldGetSingleEntry() throws Exception {
        when(merklisteService.getEintrag(EMAIL, ENTRY_ID)).thenReturn(response());

        mockMvc.perform(get("/api/v1/me/merkliste/{entryId}", ENTRY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titel").value("Praktikum Softwareentwicklung"));

        verify(merklisteService).getEintrag(EMAIL, ENTRY_ID);
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void shouldAddEntry() throws Exception {
        when(merklisteService.addEintrag(eq(EMAIL), any(MerklisteRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/me/merkliste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"stelleId":"%s"}
                                """.formatted(INTERNSHIP_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/me/merkliste/" + ENTRY_ID))
                .andExpect(jsonPath("$.id").value(ENTRY_ID.toString()));

        verify(merklisteService).addEintrag(eq(EMAIL), any(MerklisteRequest.class));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void shouldUpdateEntry() throws Exception {
        when(merklisteService.updateEintrag(eq(EMAIL), eq(ENTRY_ID), any(MerklisteRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/v1/me/merkliste/{entryId}", ENTRY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"stelleId":"%s"}
                                """.formatted(INTERNSHIP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stelleId").value(INTERNSHIP_ID.toString()));

        verify(merklisteService).updateEintrag(eq(EMAIL), eq(ENTRY_ID), any(MerklisteRequest.class));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "STUDENT")
    void shouldDeleteEntry() throws Exception {
        mockMvc.perform(delete("/api/v1/me/merkliste/{entryId}", ENTRY_ID))
                .andExpect(status().isNoContent());

        verify(merklisteService).deleteEintrag(EMAIL, ENTRY_ID);
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/merkliste"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldDenyAccessToAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/me/merkliste"))
                .andExpect(status().isForbidden());
    }

    private static MerklisteResponse response() {
        return new MerklisteResponse(
                ENTRY_ID,
                INTERNSHIP_ID,
                "Praktikum Softwareentwicklung",
                "Beispiel AG",
                "Bern",
                LocalDateTime.of(2026, 10, 20, 10, 0)
        );
    }
}
