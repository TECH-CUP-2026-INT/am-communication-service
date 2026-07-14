package co.edu.escuelaing.techcup.communications.dto;

import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportTicketStatus;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        UUID chatId,
        UUID requesterId,
        String subject,
        SupportTicketStatus status,
        SupportLevel currentLevel,
        UUID assignedTo,
        Instant createdAt,
        Instant updatedAt
) {
}
