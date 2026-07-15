package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ResolveReportCommand;

public interface ResolveReportUseCase {

    ReportedMessage resolve(ResolveReportCommand command);
}
