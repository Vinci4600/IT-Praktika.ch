package ch.itpraktika.api.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank @Size(max = 128) String token,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
