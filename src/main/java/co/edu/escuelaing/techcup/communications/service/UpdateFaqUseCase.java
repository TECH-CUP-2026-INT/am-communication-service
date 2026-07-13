package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.service.command.UpdateFaqCommand;

public interface UpdateFaqUseCase {

    Faq update(UpdateFaqCommand command);
}
