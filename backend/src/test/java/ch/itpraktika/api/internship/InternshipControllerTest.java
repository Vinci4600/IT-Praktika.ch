package ch.itpraktika.api.internship;

import ch.itpraktika.api.config.SecurityConfig;
import ch.itpraktika.api.user.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternshipController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class InternshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternshipService internshipService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void shouldListPublishedInternshipsWithSearchFilters() throws Exception {
        when(internshipService.listPublished("software", "ICT Fachmann", "Bern", 1, 20))
                .thenReturn(Map.of(
                        "data", java.util.List.of(),
                        "page", 1,
                        "size", 20,
                        "totalElements", 0,
                        "totalPages", 0
                ));

        mockMvc.perform(get("/api/v1/stellen")
                        .param("suche", "software")
                        .param("beruf", "ICT Fachmann")
                        .param("standort", "Bern")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page").value(1));

        verify(internshipService).listPublished("software", "ICT Fachmann", "Bern", 1, 20);
    }

    @Test
    void shouldRejectInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/stellen").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
