package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateSupportTicketCommand;

public interface CreateSupportTicketUseCase {

    SupportTicket create(CreateSupportTicketCommand command);
}
