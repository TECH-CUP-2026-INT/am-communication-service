package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.service.command.CreateChatCommand;

public interface CreateChatUseCase {

    Chat create(CreateChatCommand command);
}
