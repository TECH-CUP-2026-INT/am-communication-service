package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ParticipantRequest(
        @Schema(description = "Identificador del usuario participante", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El identificador del usuario es obligatorio") UUID userId,

        @Schema(description = "Rol del participante en el chat", example = "MEMBER")
        @NotNull(message = "El rol del participante es obligatorio") ParticipantRole role
) {
}
