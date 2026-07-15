package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.communications.domain.model.enums.MessageStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        @Schema(description = "Identificador del mensaje", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Identificador del chat al que pertenece", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c") UUID chatId,
        @Schema(description = "Identificador de quien envía el mensaje", example = "660e8400-e29b-41d4-a716-446655440001") UUID senderId,
        @Schema(description = "Contenido del mensaje", example = "Hola, necesito ayuda con mi reserva") String content,
        @Schema(description = "Estado del mensaje", example = "SENT") MessageStatus status,
        @Schema(description = "Fecha y hora de envío", example = "2026-07-15T10:15:30Z") Instant sentAt
) {
}
