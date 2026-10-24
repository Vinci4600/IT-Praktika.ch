package ch.itpraktika.api.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler exceptionHandler = new ApiExceptionHandler();

    @Test
    void shouldReturnNotFoundResponseForNotFoundException() {
        ResponseStatusException exception =
                new ResponseStatusException(NOT_FOUND, "Die Stelle wurde nicht gefunden.");

        var response = exceptionHandler.handleStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", Map.of(
                        "code", "NOT_FOUND",
                        "message", "Die Stelle wurde nicht gefunden."
                ));
    }

    @Test
    void shouldReturnBadRequestForValidationException() {
        var response = exceptionHandler.handleValidationException(
                new IllegalArgumentException("Ungültige Anfrage")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "Die Anfrage enthält ungültige Angaben."
                ));
    }
}
