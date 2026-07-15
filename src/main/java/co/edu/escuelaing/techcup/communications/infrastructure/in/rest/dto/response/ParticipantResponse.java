package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ParticipantResponse(
        @Schema(description = "Identificador del participante", example = "770e8400-e29b-41d4-a716-446655440002") UUID id,
        @Schema(description = "Identificador del usuario", example = "550e8400-e29b-41d4-a716-446655440000") UUID userId,
        @Schema(description = "Rol del participante en el chat", example = "MEMBER") ParticipantRole role,
        @Schema(description = "Fecha y hora de ingreso al chat", example = "2026-07-15T10:15:30Z") Instant joinedAt
) {
}
