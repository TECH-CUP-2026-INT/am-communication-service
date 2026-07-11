package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportedMessageTest {

    private final UUID sender = UUID.randomUUID();
    private final UUID reporter = UUID.randomUUID();

    private Message aMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat.postMessage(sender, "offensive text");
    }

    @Test
    void createsPendingReport() {
        ReportedMessage report = ReportedMessage.create(aMessage(), reporter, "spam");

        assertThat(report.getId()).isNotNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getReporterId()).isEqualTo(reporter);
        assertThat(report.getMessageId()).isNotNull();
        assertThat(report.getReviewedAt()).isNull();
    }

    @Test
    void rejectsBlankReason() {
        Message message = aMessage();

        assertThatThrownBy(() -> ReportedMessage.create(message, reporter, ""))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void resolvesWithTerminalStatus() {
        ReportedMessage report = ReportedMessage.create(aMessage(), reporter, "spam");

        report.resolve(ReportStatus.ACTIONED, "message deleted");

        assertThat(report.getStatus()).isEqualTo(ReportStatus.ACTIONED);
        assertThat(report.getResolution()).isEqualTo("message deleted");
        assertThat(report.getReviewedAt()).isNotNull();
    }

    @Test
    void rejectsResolutionIntoPending() {
        ReportedMessage report = ReportedMessage.create(aMessage(), reporter, "spam");

        assertThatThrownBy(() -> report.resolve(ReportStatus.PENDING, "x"))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsResolvingTwice() {
        ReportedMessage report = ReportedMessage.create(aMessage(), reporter, "spam");
        report.resolve(ReportStatus.DISMISSED, "no violation");

        assertThatThrownBy(() -> report.resolve(ReportStatus.REVIEWED, "again"))
                .isInstanceOf(InvalidChatOperationException.class);
    }
}
