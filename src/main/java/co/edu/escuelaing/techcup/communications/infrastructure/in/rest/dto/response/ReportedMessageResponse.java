package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ReportedMessageResponse(
        @Schema(description = "Identificador del reporte", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Identificador del mensaje reportado", example = "770e8400-e29b-41d4-a716-446655440002") UUID messageId,
        @Schema(description = "Identificador de quien reporta el mensaje", example = "660e8400-e29b-41d4-a716-446655440001") UUID reporterId,
        @Schema(description = "Motivo del reporte", example = "Contenido ofensivo") String reason,
        @Schema(description = "Estado del reporte", example = "RESOLVED") ReportStatus status,
        @Schema(description = "Resolución dada por el moderador", example = "Se eliminó el mensaje") String resolution,
        @Schema(description = "Fecha y hora de creación del reporte", example = "2026-07-15T10:15:30Z") Instant createdAt,
        @Schema(description = "Fecha y hora en que se revisó el reporte", example = "2026-07-15T11:00:00Z") Instant reviewedAt
) {
}
