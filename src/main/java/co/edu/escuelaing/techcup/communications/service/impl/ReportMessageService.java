package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.service.ReportMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReportMessageCommand;
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
            throw new InvalidChatOperationException("Cannot report a deleted message: " + command.messageId());
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
