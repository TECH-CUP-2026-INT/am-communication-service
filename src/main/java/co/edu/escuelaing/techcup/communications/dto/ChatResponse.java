package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        ChatType type,
        UUID teamId,
        ChatStatus status,
        List<ParticipantResponse> participants,
        Instant createdAt
) {
}
