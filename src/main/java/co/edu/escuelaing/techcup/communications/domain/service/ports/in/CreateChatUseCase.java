package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateChatCommand;

public interface CreateChatUseCase {

    Chat create(CreateChatCommand command);
}
