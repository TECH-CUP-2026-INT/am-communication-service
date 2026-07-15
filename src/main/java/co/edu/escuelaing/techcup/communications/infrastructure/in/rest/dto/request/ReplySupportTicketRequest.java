package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplySupportTicketRequest(
        @Schema(description = "Contenido de la respuesta", example = "Ya revisamos tu caso, por favor intenta de nuevo")
        @NotBlank(message = "El contenido de la respuesta no puede estar vacío")
        @Size(max = Message.MAX_CONTENT_LENGTH, message = "El contenido no puede superar {max} caracteres")
        String content
) {
}
