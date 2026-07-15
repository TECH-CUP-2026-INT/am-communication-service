package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        @Schema(description = "Identificador del ticket de soporte", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Identificador del chat asociado al ticket", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c") UUID chatId,
        @Schema(description = "Identificador de quien solicita el soporte", example = "660e8400-e29b-41d4-a716-446655440001") UUID requesterId,
        @Schema(description = "Asunto del ticket", example = "No puedo ingresar al torneo") String subject,
        @Schema(description = "Estado del ticket", example = "OPEN") SupportTicketStatus status,
        @Schema(description = "Nivel de soporte que atiende actualmente el ticket", example = "FAQ") SupportLevel currentLevel,
        @Schema(description = "Identificador de quien tiene asignado el ticket", example = "880e8400-e29b-41d4-a716-446655440003") UUID assignedTo,
        @Schema(description = "Fecha y hora de creación", example = "2026-07-15T10:15:30Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última actualización", example = "2026-07-15T10:15:30Z") Instant updatedAt
) {
}
