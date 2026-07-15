package co.edu.escuelaing.techcup.communications.application.usecase.command;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;

import java.util.UUID;

public record ResolveReportCommand(
        UUID reportId,
        UUID moderatorId,
        ReportStatus resolutionStatus,
        String note,
        ModeratorActionType actionType
) {
}
