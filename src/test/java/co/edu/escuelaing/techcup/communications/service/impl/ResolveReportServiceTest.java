package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ModeratorAction;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.ReportedMessageNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.repository.ModeratorActionRepository;
import co.edu.escuelaing.techcup.communications.repository.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.service.command.ResolveReportCommand;
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
class ResolveReportServiceTest {

    @Mock
    private ReportedMessageRepository reportedMessageRepository;

    @Mock
    private ModeratorActionRepository moderatorActionRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ResolveReportService service;

    private final UUID moderator = UUID.randomUUID();
    private final UUID reporter = UUID.randomUUID();

    private ReportedMessage pendingReport() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        UUID sender = UUID.randomUUID();
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        Message message = chat.postMessage(sender, "offensive");
        message.markReported();
        return ReportedMessage.create(message, reporter, "spam");
    }

    @Test
    void dismissRecordsActionWithoutDeletingMessage() {
        ReportedMessage report = pendingReport();
        when(reportedMessageRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(reportedMessageRepository.save(any(ReportedMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        ReportedMessage result = service.resolve(new ResolveReportCommand(
                report.getId(), moderator, ReportStatus.DISMISSED, "no violation", ModeratorActionType.WARN));

        assertThat(result.getStatus()).isEqualTo(ReportStatus.DISMISSED);
        verify(moderatorActionRepository).save(any(ModeratorAction.class));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void actionedWithDeleteMarksMessageDeleted() {
        ReportedMessage report = pendingReport();
        when(reportedMessageRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(reportedMessageRepository.save(any(ReportedMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        service.resolve(new ResolveReportCommand(
                report.getId(), moderator, ReportStatus.ACTIONED, "removed", ModeratorActionType.DELETE_MESSAGE));

        assertThat(report.getMessage().getStatus()).isEqualTo(MessageStatus.DELETED);
        verify(messageRepository).save(report.getMessage());
        verify(moderatorActionRepository).save(any(ModeratorAction.class));
    }

    @Test
    void throwsWhenReportNotFound() {
        UUID id = UUID.randomUUID();
        when(reportedMessageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve(new ResolveReportCommand(
                id, moderator, ReportStatus.DISMISSED, "x", ModeratorActionType.WARN)))
                .isInstanceOf(ReportedMessageNotFoundException.class);
        verify(moderatorActionRepository, never()).save(any());
    }

    @Test
    void rejectsResolvingAlreadyResolvedReport() {
        ReportedMessage report = pendingReport();
        report.resolve(ReportStatus.REVIEWED, "done");
        when(reportedMessageRepository.findById(report.getId())).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.resolve(new ResolveReportCommand(
                report.getId(), moderator, ReportStatus.DISMISSED, "again", ModeratorActionType.WARN)))
                .isInstanceOf(InvalidChatOperationException.class);
        verify(moderatorActionRepository, never()).save(any());
    }
}
