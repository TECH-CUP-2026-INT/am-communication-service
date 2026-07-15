package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportMessageRequest(
        @Schema(description = "Motivo del reporte", example = "Contenido ofensivo")
        @NotBlank(message = "El motivo del reporte es obligatorio")
        @Size(max = ReportedMessage.MAX_REASON_LENGTH, message = "El motivo no puede superar {max} caracteres")
        String reason
) {
}
