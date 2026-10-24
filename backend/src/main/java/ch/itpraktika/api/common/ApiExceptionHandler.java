package ch.itpraktika.api.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", Map.of(
                        "code", "INVALID_CREDENTIALS",
                        "message", "E-Mail oder Passwort ist ungültig."
                )
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatusException(
            ResponseStatusException exception
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status).body(Map.of(
                "error", Map.of(
                        "code", status == HttpStatus.NOT_FOUND
                                ? "NOT_FOUND"
                                : status == HttpStatus.CONFLICT
                                        ? "CONFLICT"
                                        : status == HttpStatus.TOO_MANY_REQUESTS ? "RATE_LIMITED" : "REQUEST_ERROR",
                        "message", exception.getReason() == null ? status.getReasonPhrase() : exception.getReason()
                )
        ));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> handleValidationException(
            Exception exception
    ) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "Die Anfrage enthält ungültige Angaben."
                )
        ));
    }
}
