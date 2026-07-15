package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReportMessageCommand;

public interface ReportMessageUseCase {

    ReportedMessage report(ReportMessageCommand command);
}
