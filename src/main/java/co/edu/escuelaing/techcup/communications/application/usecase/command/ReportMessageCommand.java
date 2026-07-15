package co.edu.escuelaing.techcup.communications.application.usecase.command;

import java.util.UUID;

public record ReportMessageCommand(UUID messageId, UUID reporterId, String reason) {
}
