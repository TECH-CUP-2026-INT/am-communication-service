package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReportedMessageResponse(
        UUID id,
        UUID messageId,
        UUID reporterId,
        String reason,
        ReportStatus status,
        String resolution,
        Instant createdAt,
        Instant reviewedAt
) {
}
