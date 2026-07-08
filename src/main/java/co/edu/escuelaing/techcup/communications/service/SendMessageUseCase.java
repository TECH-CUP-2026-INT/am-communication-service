package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;

public interface SendMessageUseCase {

    Message send(SendMessageCommand command);
}
