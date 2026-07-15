package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportTicketRequest(
        @Schema(description = "Asunto del ticket de soporte", example = "No puedo ingresar al torneo")
        @NotBlank(message = "El asunto del ticket es obligatorio")
        @Size(max = SupportTicket.MAX_SUBJECT_LENGTH, message = "El asunto no puede superar {max} caracteres")
        String subject
) {
}
