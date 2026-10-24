package ch.itpraktika.api;

import ch.itpraktika.api.user.AuthenticationRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class DatabaseMigrationIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("it_praktika_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuthenticationRateLimitService rateLimitService;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("app.jwt.secret", () -> "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        registry.add("spring.mail.host", () -> "localhost");
    }

    @BeforeEach
    void clearData() {
        jdbcTemplate.update("DELETE FROM internships");
        jdbcTemplate.update("DELETE FROM companies WHERE name = 'Beispiel AG'");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM revoked_tokens");
        jdbcTemplate.update("DELETE FROM authentication_rate_limits");
    }

    @Test
    void shouldApplyAllFlywayMigrationsAndValidateTheSchema() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE",
                Integer.class
        );

        assertThat(appliedMigrations).isGreaterThanOrEqualTo(6);
        assertThat(tableExists("merkliste_entries")).isTrue();
        assertThat(tableExists("password_reset_tokens")).isTrue();
        assertThat(tableExists("revoked_tokens")).isTrue();
        assertThat(tableExists("authentication_rate_limits")).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE initiative_listed = TRUE",
                Integer.class
        )).isEqualTo(84);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE initiative_listed = TRUE AND email IS NOT NULL",
                Integer.class
        )).isEqualTo(74);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE initiative_listed = TRUE AND internship_url IS NOT NULL",
                Integer.class
        )).isEqualTo(24);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE initiative_listed = TRUE AND is_partner = FALSE",
                Integer.class
        )).isEqualTo(84);
    }

    @Test
    void shouldPersistFavoriteAndCascadeDeleteItWithTheStudentAccount() {
        String userId = UUID.randomUUID().toString();
        String companyId = UUID.randomUUID().toString();
        String internshipId = UUID.randomUUID().toString();
        String entryId = UUID.randomUUID().toString();
        insertUser(userId);
        insertCompany(companyId);
        insertInternship(internshipId, companyId);

        jdbcTemplate.update(
                "INSERT INTO merkliste_entries (id, user_id, internship_id) VALUES (?, ?, ?)",
                entryId,
                userId,
                internshipId
        );

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM merkliste_entries WHERE user_id = ?",
                Integer.class,
                userId
        )).isEqualTo(1);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO merkliste_entries (id, user_id, internship_id) VALUES (?, ?, ?)",
                UUID.randomUUID().toString(),
                userId,
                internshipId
        )).isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM merkliste_entries WHERE id = ?",
                Integer.class,
                entryId
        )).isZero();
    }

    @Test
    void shouldPersistAndEnforceAuthenticationRateLimits() {
        rateLimitService.check("integration-test", "127.0.0.1", "student@example.com", 1, Duration.ofMinutes(1));

        assertThatThrownBy(() -> rateLimitService.check(
                "integration-test",
                "127.0.0.1",
                "student@example.com",
                1,
                Duration.ofMinutes(1)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Zu viele Versuche");
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void insertUser(String id) {
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, role) VALUES (?, ?, ?, 'student')",
                id,
                UUID.randomUUID() + "@example.com",
                "$2a$10$test-hash"
        );
    }

    private void insertCompany(String id) {
        jdbcTemplate.update(
                """
                INSERT INTO companies (id, name, location, specialties)
                VALUES (?, 'Beispiel AG', 'Bern', JSON_ARRAY('Software'))
                """,
                id
        );
    }

    private void insertInternship(String id, String companyId) {
        jdbcTemplate.update(
                """
                INSERT INTO internships (
                  id, company_id, company_name, title, location, duration, start_option,
                  career_field, contact_email, compensation, description, requirements, status
                ) VALUES (?, ?, 'Beispiel AG', 'Praktikum', 'Bern', '6 Monate', 'by_agreement',
                          'ICT Fachmann', 'jobs@example.com', 'nicht angegeben', 'Beschreibung',
                          JSON_ARRAY('Interesse'), 'published')
                """,
                id,
                companyId
        );
    }
}
