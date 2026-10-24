package ch.itpraktika.api.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void revoke(JwtAuthenticationToken authentication) {
        String tokenId = authentication.getToken().getId();
        var expiresAt = authentication.getToken().getExpiresAt();
        if (tokenId == null || expiresAt == null) {
            throw new IllegalStateException("Der angemeldete Token hat keine ID oder Ablaufzeit.");
        }
        revokedTokenRepository.save(new RevokedToken(tokenId, expiresAt));
    }
}
