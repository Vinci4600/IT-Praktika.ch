package ch.itpraktika.api.user;

import ch.itpraktika.api.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String TEST_SECRET = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private final byte[] secret = Base64.getDecoder().decode(TEST_SECRET);
    private final JwtTokenService tokenService = new JwtTokenService(
            new SecurityConfig().jwtEncoder(TEST_SECRET)
    );
    private final JwtDecoder jwtDecoder = NimbusJwtDecoder
            .withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

    @Test
    void shouldIssueSignedTokenWithSubjectRoleAndExpiration() {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "admin@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        String token = tokenService.createToken(authentication);
        var decodedToken = jwtDecoder.decode(token);

        assertThat(decodedToken.getSubject()).isEqualTo("admin@example.com");
        assertThat(decodedToken.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(decodedToken.getClaimAsString("iss")).isEqualTo(JwtTokenService.ISSUER);
        assertThat(decodedToken.getExpiresAt())
                .isAfter(Instant.now())
                .isBefore(Instant.now().plusSeconds(JwtTokenService.TOKEN_LIFETIME_SECONDS + 1));
    }
}
