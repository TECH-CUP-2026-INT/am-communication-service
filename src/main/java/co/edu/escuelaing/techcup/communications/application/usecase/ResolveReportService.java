package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.ModeratorAction;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.exception.ReportedMessageNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ModeratorActionRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ResolveReportUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ResolveReportCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolveReportService implements ResolveReportUseCase {

    private static final String TARGET_TYPE_MESSAGE = "MESSAGE";

    private final ReportedMessageRepository reportedMessageRepository;
    private final ModeratorActionRepository moderatorActionRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ReportedMessage resolve(ResolveReportCommand command) {
        ReportedMessage report = reportedMessageRepository.findById(command.reportId())
                .orElseThrow(() -> new ReportedMessageNotFoundException(command.reportId()));

        // Domain enforces PENDING -> terminal transition.
        report.resolve(command.resolutionStatus(), command.note());

        moderatorActionRepository.save(ModeratorAction.of(
                command.moderatorId(),
                TARGET_TYPE_MESSAGE,
                report.getMessageId(),
                command.actionType(),
                command.note()));

        if (command.actionType() == ModeratorActionType.DELETE_MESSAGE) {
            report.getMessage().markDeleted();
            messageRepository.save(report.getMessage());
        }
        return reportedMessageRepository.save(report);
    }
}
