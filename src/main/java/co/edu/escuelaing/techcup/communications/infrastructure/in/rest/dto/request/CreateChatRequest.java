package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateChatRequest(
        @Schema(description = "Tipo de chat", example = "DIRECT")
        @NotNull(message = "El tipo de chat es obligatorio") ChatType type,

        @Schema(description = "Identificador del equipo, si el chat pertenece a uno", example = "660e8400-e29b-41d4-a716-446655440001")
        UUID teamId,

        @Schema(description = "Participantes iniciales del chat")
        @NotEmpty(message = "Debe indicar al menos un participante") @Valid List<ParticipantRequest> participants
) {
}
