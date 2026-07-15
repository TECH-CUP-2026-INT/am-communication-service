package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatStatus;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        @Schema(description = "Identificador del chat", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c") UUID id,
        @Schema(description = "Tipo de chat", example = "DIRECT") ChatType type,
        @Schema(description = "Identificador del equipo, si el chat pertenece a uno", example = "660e8400-e29b-41d4-a716-446655440001") UUID teamId,
        @Schema(description = "Estado actual del chat", example = "OPEN") ChatStatus status,
        @Schema(description = "Participantes del chat") List<ParticipantResponse> participants,
        @Schema(description = "Fecha y hora de creación", example = "2026-07-15T10:15:30Z") Instant createdAt
) {
}
