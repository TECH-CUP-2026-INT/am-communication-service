package co.edu.escuelaing.techcup.communications.service.command;

import java.util.UUID;

public record CreateSupportTicketCommand(UUID requesterId, String subject) {
}
