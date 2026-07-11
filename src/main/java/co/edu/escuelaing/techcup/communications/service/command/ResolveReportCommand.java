package co.edu.escuelaing.techcup.communications.service.command;

import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;

import java.util.UUID;

public record ResolveReportCommand(
        UUID reportId,
        UUID moderatorId,
        ReportStatus resolutionStatus,
        String note,
        ModeratorActionType actionType
) {
}
