package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.domain.model.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReportMessageUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReportMessageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportMessageService implements ReportMessageUseCase {

    private final MessageRepository messageRepository;
    private final ReportedMessageRepository reportedMessageRepository;

    @Override
    @Transactional
    public ReportedMessage report(ReportMessageCommand command) {
        Message message = messageRepository.findById(command.messageId())
                .orElseThrow(() -> new MessageNotFoundException(command.messageId()));

        if (message.getStatus() == MessageStatus.DELETED) {
            throw new InvalidChatOperationException("No se puede reportar un mensaje eliminado: " + command.messageId());
        }
        if (reportedMessageRepository.existsByMessage_IdAndReporterId(command.messageId(), command.reporterId())) {
            throw new MessageAlreadyReportedException(command.messageId());
        }

        ReportedMessage report = reportedMessageRepository.save(
                ReportedMessage.create(message, command.reporterId(), command.reason()));

        // Flag the message once; further distinct reports keep the same REPORTED status.
        if (message.getStatus() == MessageStatus.SENT) {
            message.markReported();
            messageRepository.save(message);
        }
        return report;
    }
}
