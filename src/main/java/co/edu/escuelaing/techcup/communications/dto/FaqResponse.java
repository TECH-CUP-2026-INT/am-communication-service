package co.edu.escuelaing.techcup.communications.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record FaqResponse(
        UUID id,
        Set<String> keywords,
        String answer,
        Instant createdAt,
        Instant updatedAt
) {
}
