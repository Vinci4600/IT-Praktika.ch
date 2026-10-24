package ch.itpraktika.api.internship;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InternshipRepository extends JpaRepository<Internship, String>,
        JpaSpecificationExecutor<Internship> {

    Page<Internship> findAllByStatus(Internship.InternshipStatus status, Pageable pageable);

    Optional<Internship> findByIdAndStatus(String id, Internship.InternshipStatus status);
}
