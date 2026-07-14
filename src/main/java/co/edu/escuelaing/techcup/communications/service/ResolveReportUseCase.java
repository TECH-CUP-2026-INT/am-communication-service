package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.service.command.ResolveReportCommand;

public interface ResolveReportUseCase {

    ReportedMessage resolve(ResolveReportCommand command);
}
