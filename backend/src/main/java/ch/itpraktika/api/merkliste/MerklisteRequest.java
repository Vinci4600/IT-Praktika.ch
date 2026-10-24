package ch.itpraktika.api.merkliste;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MerklisteRequest(
        @NotNull UUID stelleId
) {
}
