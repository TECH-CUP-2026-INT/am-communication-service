package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.service.command.CreateSupportTicketCommand;

public interface CreateSupportTicketUseCase {

    SupportTicket create(CreateSupportTicketCommand command);
}
