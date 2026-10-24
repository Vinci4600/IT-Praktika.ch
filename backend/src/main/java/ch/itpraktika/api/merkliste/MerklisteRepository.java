package ch.itpraktika.api.merkliste;

import ch.itpraktika.api.internship.Internship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerklisteRepository extends JpaRepository<MerklisteEintrag, String> {

    List<MerklisteEintrag> findAllByUser_IdAndInternship_StatusOrderByCreatedAtDesc(
            String userId,
            Internship.InternshipStatus status
    );

    Optional<MerklisteEintrag> findByIdAndUser_Id(String id, String userId);

    Optional<MerklisteEintrag> findByIdAndUser_IdAndInternship_Status(
            String id,
            String userId,
            Internship.InternshipStatus status
    );

    boolean existsByUser_IdAndInternship_Id(String userId, String internshipId);

    boolean existsByUser_IdAndInternship_IdAndIdNot(String userId, String internshipId, String id);
}
