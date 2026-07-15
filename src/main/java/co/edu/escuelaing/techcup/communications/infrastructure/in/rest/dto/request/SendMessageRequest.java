package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** The sender is never supplied by the client: it is taken from the JWT. */
public record SendMessageRequest(
        @Schema(description = "Identificador del chat destino", example = "f18c1f66-87b4-4eaf-bfdb-c6fd0fb89d7c")
        @NotNull(message = "El identificador del chat es obligatorio") UUID chatId,

        @Schema(description = "Contenido del mensaje", example = "Hola, necesito ayuda con mi reserva")
        @NotBlank(message = "El contenido del mensaje no puede estar vacío")
        @Size(max = Message.MAX_CONTENT_LENGTH, message = "El contenido del mensaje no puede superar {max} caracteres")
        String content
) {
}
