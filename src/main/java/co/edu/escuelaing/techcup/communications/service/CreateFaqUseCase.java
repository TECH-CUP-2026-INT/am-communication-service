package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.service.command.CreateFaqCommand;

public interface CreateFaqUseCase {

    Faq create(CreateFaqCommand command);
}
