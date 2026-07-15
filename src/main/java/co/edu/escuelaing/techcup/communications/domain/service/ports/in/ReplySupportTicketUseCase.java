package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReplySupportTicketCommand;

public interface ReplySupportTicketUseCase {

    Message reply(ReplySupportTicketCommand command);
}
