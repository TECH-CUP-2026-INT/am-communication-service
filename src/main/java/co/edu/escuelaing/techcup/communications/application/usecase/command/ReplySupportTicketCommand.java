package co.edu.escuelaing.techcup.communications.application.usecase.command;

import java.util.UUID;

public record ReplySupportTicketCommand(UUID ticketId, UUID senderId, String content) {
}
