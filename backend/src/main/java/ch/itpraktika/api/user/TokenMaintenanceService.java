package ch.itpraktika.api.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenMaintenanceService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final AuthenticationRateLimitService rateLimitService;

    public TokenMaintenanceService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            RevokedTokenRepository revokedTokenRepository,
            AuthenticationRateLimitService rateLimitService
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.rateLimitService = rateLimitService;
    }

    @Scheduled(fixedDelayString = "PT1H")
    public void removeExpiredSecurityData() {
        Instant now = Instant.now();
        passwordResetTokenRepository.deleteByExpiresAtBefore(now);
        revokedTokenRepository.deleteByExpiresAtBefore(now);
        rateLimitService.removeExpired(now);
    }
}
