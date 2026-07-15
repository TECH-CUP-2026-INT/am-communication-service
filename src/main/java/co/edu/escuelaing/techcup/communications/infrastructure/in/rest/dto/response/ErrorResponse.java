package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        @Schema(description = "Fecha y hora en que ocurrió el error", example = "2026-07-15T10:15:30Z") Instant timestamp,
        @Schema(description = "Código de estado HTTP", example = "404") int status,
        @Schema(description = "Descripción corta del estado HTTP, en español", example = "No encontrado") String error,
        @Schema(description = "Mensaje descriptivo del error, en español", example = "No se encontró el chat: f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c") String message,
        @Schema(description = "Ruta donde ocurrió el error", example = "/chats/f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c") String path,
        @Schema(description = "Errores de validación por campo, cuando aplica") List<FieldError> fieldErrors
) {
    public record FieldError(
            @Schema(description = "Nombre del campo con error", example = "content") String field,
            @Schema(description = "Mensaje de error específico del campo, en español", example = "El contenido del mensaje no puede estar vacío") String message
    ) {
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
