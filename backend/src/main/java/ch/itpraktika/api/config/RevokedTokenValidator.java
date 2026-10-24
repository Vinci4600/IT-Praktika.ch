package ch.itpraktika.api.config;

import ch.itpraktika.api.user.RevokedTokenRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

public class RevokedTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenValidator(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenId = token.getId();
        boolean revoked = tokenId == null
                || revokedTokenRepository.existsByTokenIdAndExpiresAtAfter(tokenId, Instant.now());
        if (revoked) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "The access token is no longer valid.", null)
            );
        }
        return OAuth2TokenValidatorResult.success();
    }
}
