package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.domain.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReportMessageCommand;
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
        when(reportedMessageRepository.existsByMessageIdAndReporterId(message.getId(), reporter)).thenReturn(false);
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

        ReportMessageCommand command = new ReportMessageCommand(id, reporter, "spam");
        assertThatThrownBy(() -> service.report(command))
                .isInstanceOf(MessageNotFoundException.class);
        verify(reportedMessageRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateReportFromSameReporter() {
        Message message = aMessage();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(reportedMessageRepository.existsByMessageIdAndReporterId(message.getId(), reporter)).thenReturn(true);

        ReportMessageCommand command = new ReportMessageCommand(message.getId(), reporter, "spam");
        assertThatThrownBy(() -> service.report(command))
                .isInstanceOf(MessageAlreadyReportedException.class);
        verify(reportedMessageRepository, never()).save(any());
    }

    @Test
    void secondDistinctReporterDoesNotReflagAlreadyReportedMessage() {
        Message message = aMessage();
        message.markReported();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(reportedMessageRepository.existsByMessageIdAndReporterId(message.getId(), reporter)).thenReturn(false);
        when(reportedMessageRepository.save(any(ReportedMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        service.report(new ReportMessageCommand(message.getId(), reporter, "spam"));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void rejectsReportingDeletedMessage() {
        Message message = aMessage();
        message.markDeleted();
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        ReportMessageCommand command = new ReportMessageCommand(message.getId(), reporter, "spam");
        assertThatThrownBy(() -> service.report(command))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(reportedMessageRepository, never()).save(any());
    }
}
