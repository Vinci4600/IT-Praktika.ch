package ch.itpraktika.api.merkliste;

import java.time.LocalDateTime;
import java.util.UUID;

public record MerklisteResponse(
        UUID id,
        UUID stelleId,
        String titel,
        String firma,
        String standort,
        LocalDateTime gemerktAm
) {
}
