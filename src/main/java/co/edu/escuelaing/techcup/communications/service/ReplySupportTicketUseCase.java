package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;

public interface ReplySupportTicketUseCase {

    Message reply(ReplySupportTicketCommand command);
}
