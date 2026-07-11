package co.edu.escuelaing.techcup.communications.service.command;

import java.util.UUID;

public record ReplySupportTicketCommand(UUID ticketId, UUID senderId, String content) {
}
