package co.edu.escuelaing.techcup.communications.application.usecase.command;

import java.util.UUID;

public record CreateSupportTicketCommand(UUID requesterId, String subject) {
}
