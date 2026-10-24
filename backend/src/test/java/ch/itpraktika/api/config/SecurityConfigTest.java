package ch.itpraktika.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @Test
    void shouldEncodePasswordAndVerifyItWithoutStoringPlainText() {
        String plainPassword = "correct-horse-battery-staple";

        String passwordHash = passwordEncoder.encode(plainPassword);

        assertThat(passwordHash).isNotEqualTo(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, passwordHash)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", passwordHash)).isFalse();
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String firstHash = passwordEncoder.encode("same-password");
        String secondHash = passwordEncoder.encode("same-password");

        assertThat(firstHash).isNotEqualTo(secondHash);
        assertThat(passwordEncoder.matches("same-password", firstHash)).isTrue();
        assertThat(passwordEncoder.matches("same-password", secondHash)).isTrue();
    }
}
