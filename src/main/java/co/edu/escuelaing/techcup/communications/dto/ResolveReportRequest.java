package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(
        @NotNull ReportStatus resolutionStatus,
        @Size(max = 500) String note,
        @NotNull ModeratorActionType actionType
) {
}
