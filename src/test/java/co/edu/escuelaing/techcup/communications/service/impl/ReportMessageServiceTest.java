package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.service.command.ReportMessageCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReportedMessageRepository reportedMessageRepository;

    @InjectMocks
    private ReportMessageService service;

    private final UUID sender = UUID.randomUUID();
    private final UUID reporter = UUID.randomUUID();

    private Message aMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat.postMessage(sender, "offensive");
    }

    @Test
    void createsReportAndFlagsMessage() {
        Message message = aMessage();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(reportedMessageRepository.existsByMessage_IdAndReporterId(message.getId(), reporter)).thenReturn(false);
        when(reportedMessageRepository.save(any(ReportedMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportedMessage report = service.report(new ReportMessageCommand(message.getId(), reporter, "spam"));

        assertThat(report.getReporterId()).isEqualTo(reporter);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.REPORTED);
        verify(messageRepository).save(message);
    }

    @Test
    void throwsWhenMessageNotFound() {
        UUID id = UUID.randomUUID();
        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.report(new ReportMessageCommand(id, reporter, "spam")))
                .isInstanceOf(MessageNotFoundException.class);
        verify(reportedMessageRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateReportFromSameReporter() {
        Message message = aMessage();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(reportedMessageRepository.existsByMessage_IdAndReporterId(message.getId(), reporter)).thenReturn(true);

        assertThatThrownBy(() -> service.report(new ReportMessageCommand(message.getId(), reporter, "spam")))
                .isInstanceOf(MessageAlreadyReportedException.class);
        verify(reportedMessageRepository, never()).save(any());
    }

    @Test
    void secondDistinctReporterDoesNotReflagAlreadyReportedMessage() {
        Message message = aMessage();
        message.markReported();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(reportedMessageRepository.existsByMessage_IdAndReporterId(message.getId(), reporter)).thenReturn(false);
        when(reportedMessageRepository.save(any(ReportedMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        service.report(new ReportMessageCommand(message.getId(), reporter, "spam"));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void rejectsReportingDeletedMessage() {
        Message message = aMessage();
        message.markDeleted();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.report(new ReportMessageCommand(message.getId(), reporter, "spam")))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(reportedMessageRepository, never()).save(any());
    }
}
