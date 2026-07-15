package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateFaqCommand;

public interface CreateFaqUseCase {

    Faq create(CreateFaqCommand command);
}
