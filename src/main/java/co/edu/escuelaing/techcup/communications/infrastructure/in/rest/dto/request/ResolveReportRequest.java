package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(
        @Schema(description = "Estado de resolución del reporte", example = "RESOLVED")
        @NotNull(message = "El estado de resolución es obligatorio") ReportStatus resolutionStatus,

        @Schema(description = "Nota del moderador sobre la resolución", example = "Se eliminó el mensaje")
        @Size(max = 500, message = "La nota no puede superar {max} caracteres") String note,

        @Schema(description = "Acción tomada por el moderador", example = "DELETE_MESSAGE")
        @NotNull(message = "El tipo de acción es obligatorio") ModeratorActionType actionType
) {
}
