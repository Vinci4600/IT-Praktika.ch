package ch.itpraktika.api.merkliste;

import ch.itpraktika.api.internship.Internship;
import ch.itpraktika.api.internship.InternshipRepository;
import ch.itpraktika.api.user.User;
import ch.itpraktika.api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class MerklisteService {

    private final MerklisteRepository merklisteRepository;
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;

    public MerklisteService(
            MerklisteRepository merklisteRepository,
            UserRepository userRepository,
            InternshipRepository internshipRepository
    ) {
        this.merklisteRepository = merklisteRepository;
        this.userRepository = userRepository;
        this.internshipRepository = internshipRepository;
    }

    @Transactional(readOnly = true)
    public List<MerklisteResponse> getMerkliste(String email) {
        User user = getUser(email);
        return merklisteRepository.findAllByUser_IdAndInternship_StatusOrderByCreatedAtDesc(
                        user.getId(),
                        Internship.InternshipStatus.PUBLISHED
                )
                .stream()
                .map(MerklisteService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MerklisteResponse getEintrag(String email, UUID eintragId) {
        User user = getUser(email);
        return toResponse(getPublishedEintrag(user, eintragId));
    }

    @Transactional
    public MerklisteResponse addEintrag(String email, MerklisteRequest request) {
        User user = getUser(email);
        Internship internship = getPublishedInternship(request.stelleId());
        if (merklisteRepository.existsByUser_IdAndInternship_Id(user.getId(), internship.getId())) {
            throw new ResponseStatusException(CONFLICT, "Diese Stelle ist bereits in der Merkliste.");
        }
        MerklisteEintrag entry = merklisteRepository.save(new MerklisteEintrag(user, internship));
        return toResponse(entry);
    }

    @Transactional
    public MerklisteResponse updateEintrag(String email, UUID eintragId, MerklisteRequest request) {
        User user = getUser(email);
        MerklisteEintrag entry = getPublishedEintrag(user, eintragId);
        Internship internship = getPublishedInternship(request.stelleId());

        if (!entry.getInternship().getId().equals(internship.getId())
                && merklisteRepository.existsByUser_IdAndInternship_IdAndIdNot(
                        user.getId(),
                        internship.getId(),
                        entry.getId()
                )) {
            throw new ResponseStatusException(CONFLICT, "Diese Stelle ist bereits in der Merkliste.");
        }

        entry.updateInternship(internship);
        return toResponse(merklisteRepository.save(entry));
    }

    @Transactional
    public void deleteEintrag(String email, UUID eintragId) {
        User user = getUser(email);
        merklisteRepository.delete(getOwnedEintrag(user, eintragId));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Das Benutzerkonto wurde nicht gefunden."));
    }

    private MerklisteEintrag getPublishedEintrag(User user, UUID eintragId) {
        return merklisteRepository.findByIdAndUser_IdAndInternship_Status(
                        eintragId.toString(),
                        user.getId(),
                        Internship.InternshipStatus.PUBLISHED
                )
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Der Merkliste-Eintrag wurde nicht gefunden."));
    }

    private MerklisteEintrag getOwnedEintrag(User user, UUID eintragId) {
        return merklisteRepository.findByIdAndUser_Id(eintragId.toString(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Der Merkliste-Eintrag wurde nicht gefunden."));
    }

    private Internship getPublishedInternship(UUID stelleId) {
        return internshipRepository.findByIdAndStatus(
                        stelleId.toString(),
                        Internship.InternshipStatus.PUBLISHED
                )
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Die Stelle wurde nicht gefunden."));
    }

    private static MerklisteResponse toResponse(MerklisteEintrag entry) {
        Internship internship = entry.getInternship();
        return new MerklisteResponse(
                UUID.fromString(entry.getId()),
                UUID.fromString(internship.getId()),
                internship.getTitle(),
                internship.getCompanyName(),
                internship.getLocation(),
                entry.getCreatedAt()
        );
    }
}
