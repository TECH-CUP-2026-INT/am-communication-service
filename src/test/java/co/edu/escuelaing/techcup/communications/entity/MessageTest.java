package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.MessageAlreadyReportedException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageTest {

    private final UUID sender = UUID.randomUUID();

    private Chat chatWithSender() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat;
    }

    @Test
    void rejectsBlankContent() {
        Chat chat = chatWithSender();

        assertThatThrownBy(() -> chat.postMessage(sender, "   "))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void rejectsContentAboveMaxLength() {
        Chat chat = chatWithSender();
        String tooLong = "x".repeat(Message.MAX_CONTENT_LENGTH + 1);

        assertThatThrownBy(() -> chat.postMessage(sender, tooLong))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void marksMessageAsReported() {
        Message message = chatWithSender().postMessage(sender, "hi");

        message.markReported();

        assertThat(message.getStatus()).isEqualTo(MessageStatus.REPORTED);
    }

    @Test
    void rejectsReportingTwice() {
        Message message = chatWithSender().postMessage(sender, "hi");
        message.markReported();

        assertThatThrownBy(message::markReported)
                .isInstanceOf(MessageAlreadyReportedException.class);
    }

    @Test
    void rejectsReportingDeletedMessage() {
        Message message = chatWithSender().postMessage(sender, "hi");
        message.markDeleted();

        assertThatThrownBy(message::markReported)
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void marksMessageAsDeleted() {
        Message message = chatWithSender().postMessage(sender, "hi");

        message.markDeleted();

        assertThat(message.getStatus()).isEqualTo(MessageStatus.DELETED);
    }
}
