package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.application.usecase.command.UpdateFaqCommand;

public interface UpdateFaqUseCase {

    Faq update(UpdateFaqCommand command);
}
