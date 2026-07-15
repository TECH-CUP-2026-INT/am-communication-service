package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** The sender is never supplied by the client: it is taken from the STOMP session principal. */
public record SupportSendRequest(
        @Schema(description = "Identificador del ticket de soporte", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El identificador del ticket es obligatorio") UUID ticketId,

        @Schema(description = "Contenido del mensaje", example = "Gracias, ya se resolvió mi problema")
        @NotBlank(message = "El contenido del mensaje no puede estar vacío")
        @Size(max = Message.MAX_CONTENT_LENGTH, message = "El contenido del mensaje no puede superar {max} caracteres")
        String content
) {
}
