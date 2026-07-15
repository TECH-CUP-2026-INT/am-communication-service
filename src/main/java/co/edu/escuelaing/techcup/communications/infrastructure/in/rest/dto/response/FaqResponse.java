package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record FaqResponse(
        @Schema(description = "Identificador de la FAQ", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
        @Schema(description = "Palabras clave asociadas", example = "[\"horario\", \"torneo\"]") Set<String> keywords,
        @Schema(description = "Respuesta a la pregunta frecuente", example = "El torneo inicia a las 8:00 a. m.") String answer,
        @Schema(description = "Fecha y hora de creación", example = "2026-07-15T10:15:30Z") Instant createdAt,
        @Schema(description = "Fecha y hora de la última actualización", example = "2026-07-15T10:15:30Z") Instant updatedAt
) {
}
