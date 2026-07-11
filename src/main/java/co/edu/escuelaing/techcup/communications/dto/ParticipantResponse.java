package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;

import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        UUID userId,
        ParticipantRole role,
        Instant joinedAt
) {
}
