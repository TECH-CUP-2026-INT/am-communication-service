package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.service.command.ReportMessageCommand;

public interface ReportMessageUseCase {

    ReportedMessage report(ReportMessageCommand command);
}
