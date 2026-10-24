package ch.itpraktika.api.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    boolean existsByTokenIdAndExpiresAtAfter(String tokenId, Instant now);

    void deleteByExpiresAtBefore(Instant now);
}
