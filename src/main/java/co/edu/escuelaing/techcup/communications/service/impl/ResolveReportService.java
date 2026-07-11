package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.ModeratorAction;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.exception.ReportedMessageNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.ModeratorActionRepository;
import co.edu.escuelaing.techcup.communications.repository.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.service.ResolveReportUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ResolveReportCommand;
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

        moderatorActionRepository.save(ModeratorAction.record(
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
