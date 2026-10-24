package ch.itpraktika.api.merkliste;

import ch.itpraktika.api.internship.Internship;
import ch.itpraktika.api.internship.InternshipRepository;
import ch.itpraktika.api.user.User;
import ch.itpraktika.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerklisteServiceTest {

    private static final String EMAIL = "student@example.com";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String INTERNSHIP_ID = UUID.randomUUID().toString();
    private static final String ENTRY_ID = UUID.randomUUID().toString();

    private final MerklisteRepository merklisteRepository = mock(MerklisteRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final InternshipRepository internshipRepository = mock(InternshipRepository.class);
    private final MerklisteService service = new MerklisteService(
            merklisteRepository,
            userRepository,
            internshipRepository
    );
    private User student;
    private Internship internship;
    private MerklisteEintrag entry;

    @BeforeEach
    void setUp() {
        student = mock(User.class);
        internship = mock(Internship.class);
        when(student.getId()).thenReturn(USER_ID);
        when(internship.getId()).thenReturn(INTERNSHIP_ID);
        when(internship.getStatus()).thenReturn(Internship.InternshipStatus.PUBLISHED);
        when(internship.getTitle()).thenReturn("Praktikum Softwareentwicklung");
        when(internship.getCompanyName()).thenReturn("Beispiel AG");
        when(internship.getLocation()).thenReturn("Bern");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(student));
        when(internshipRepository.findByIdAndStatus(
                INTERNSHIP_ID,
                Internship.InternshipStatus.PUBLISHED
        )).thenReturn(Optional.of(internship));
        entry = new MerklisteEintrag(student, internship);
    }

    @Test
    void shouldReturnOnlyPublishedEntriesForTheCurrentStudent() {
        when(merklisteRepository.findAllByUser_IdAndInternship_StatusOrderByCreatedAtDesc(
                USER_ID,
                Internship.InternshipStatus.PUBLISHED
        )).thenReturn(List.of(entry));

        List<MerklisteResponse> result = service.getMerkliste(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().stelleId()).isEqualTo(UUID.fromString(INTERNSHIP_ID));
    }

    @Test
    void shouldAddAUniqueEntryForTheCurrentStudent() {
        when(merklisteRepository.existsByUser_IdAndInternship_Id(USER_ID, INTERNSHIP_ID)).thenReturn(false);
        when(merklisteRepository.save(any(MerklisteEintrag.class))).thenAnswer(invocation -> {
            entry = invocation.getArgument(0);
            return entry;
        });

        MerklisteResponse response = service.addEintrag(EMAIL, new MerklisteRequest(UUID.fromString(INTERNSHIP_ID)));

        assertThat(response.stelleId()).isEqualTo(UUID.fromString(INTERNSHIP_ID));
        assertThat(response.titel()).isEqualTo("Praktikum Softwareentwicklung");
        verify(merklisteRepository).save(any(MerklisteEintrag.class));
    }

    @Test
    void shouldRejectDuplicateEntry() {
        when(merklisteRepository.existsByUser_IdAndInternship_Id(USER_ID, INTERNSHIP_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.addEintrag(
                EMAIL,
                new MerklisteRequest(UUID.fromString(INTERNSHIP_ID))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits in der Merkliste");
    }

    @Test
    void shouldOnlyDeleteAnEntryOwnedByTheCurrentStudent() {
        when(merklisteRepository.findByIdAndUser_Id(ENTRY_ID, USER_ID)).thenReturn(Optional.of(entry));

        service.deleteEintrag(EMAIL, UUID.fromString(ENTRY_ID));

        verify(merklisteRepository).delete(entry);
    }
}
