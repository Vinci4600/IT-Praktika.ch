package ch.itpraktika.api.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.sql.Timestamp;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
public class AuthenticationRateLimitService {

    private final JdbcTemplate jdbcTemplate;

    public AuthenticationRateLimitService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void check(String action, String clientAddress, String identifier, int maxAttempts, Duration window) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(window);
        String rateKey = hash(action + "\u0000" + clientAddress + "\u0000" + identifier);

        jdbcTemplate.update("""
                INSERT INTO authentication_rate_limits (rate_key, attempts, window_start, expires_at)
                VALUES (?, 1, ?, ?) AS incoming
                ON DUPLICATE KEY UPDATE
                  attempts = IF(authentication_rate_limits.expires_at <= incoming.window_start,
                                1, authentication_rate_limits.attempts + 1),
                  window_start = IF(authentication_rate_limits.expires_at <= incoming.window_start,
                                    incoming.window_start, authentication_rate_limits.window_start),
                  expires_at = IF(authentication_rate_limits.expires_at <= incoming.window_start,
                                  incoming.expires_at, authentication_rate_limits.expires_at)
                """, rateKey, Timestamp.from(now), Timestamp.from(expiresAt));

        Integer attempts = jdbcTemplate.queryForObject(
                "SELECT attempts FROM authentication_rate_limits WHERE rate_key = ?",
                Integer.class,
                rateKey
        );
        if (attempts == null || attempts > maxAttempts) {
            throw new ResponseStatusException(
                    TOO_MANY_REQUESTS,
                    "Zu viele Versuche. Bitte warte kurz und versuche es später erneut."
            );
        }
    }

    public void clear(String action, String clientAddress, String identifier) {
        jdbcTemplate.update(
                "DELETE FROM authentication_rate_limits WHERE rate_key = ?",
                hash(action + "\u0000" + clientAddress + "\u0000" + identifier)
        );
    }

    public void removeExpired(Instant before) {
        jdbcTemplate.update(
                "DELETE FROM authentication_rate_limits WHERE expires_at < ?",
                Timestamp.from(before)
        );
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 ist in dieser Java-Laufzeit nicht verfügbar.", exception);
        }
    }
}
