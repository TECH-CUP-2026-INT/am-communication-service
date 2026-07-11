package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatTest {

    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();

    @Test
    void createsDirectChatOpenWithoutTeam() {
        Chat chat = Chat.create(ChatType.DIRECT, null);

        assertThat(chat.getId()).isNotNull();
        assertThat(chat.getType()).isEqualTo(ChatType.DIRECT);
        assertThat(chat.getStatus()).isEqualTo(ChatStatus.OPEN);
        assertThat(chat.getTeamId()).isNull();
        assertThat(chat.getCreatedAt()).isNotNull();
        assertThat(chat.getParticipants()).isEmpty();
    }

    @Test
    void groupChatRequiresTeamId() {
        assertThatThrownBy(() -> Chat.create(ChatType.GROUP, null))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void nonGroupChatRejectsTeamId() {
        assertThatThrownBy(() -> Chat.create(ChatType.DIRECT, UUID.randomUUID()))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void addsParticipantAndDetectsMembership() {
        Chat chat = Chat.create(ChatType.DIRECT, null);

        chat.addParticipant(userA, ParticipantRole.MEMBER);

        assertThat(chat.isParticipant(userA)).isTrue();
        assertThat(chat.isParticipant(userB)).isFalse();
        assertThat(chat.getParticipants()).hasSize(1);
    }

    @Test
    void rejectsDuplicateParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);

        assertThatThrownBy(() -> chat.addParticipant(userA, ParticipantRole.MEMBER))
                .isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void participantsCollectionIsUnmodifiable() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);

        assertThatThrownBy(() -> chat.getParticipants().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void postsMessageFromParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);

        Message message = chat.postMessage(userA, "hello");

        assertThat(message.getSenderId()).isEqualTo(userA);
        assertThat(message.getContent()).isEqualTo("hello");
        assertThat(message.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(message.getChatId()).isEqualTo(chat.getId());
    }

    @Test
    void rejectsMessageFromNonParticipant() {
        Chat chat = Chat.create(ChatType.DIRECT, null);

        assertThatThrownBy(() -> chat.postMessage(userA, "hello"))
                .isInstanceOf(ParticipantNotAllowedException.class);
    }

    @Test
    void rejectsMessageOnClosedChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);
        chat.close();

        assertThatThrownBy(() -> chat.postMessage(userA, "hello"))
                .isInstanceOf(ChatClosedException.class);
    }

    @Test
    void closingClosesTheChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);

        chat.close();

        assertThat(chat.isClosed()).isTrue();
        assertThat(chat.getStatus()).isEqualTo(ChatStatus.CLOSED);
    }

    @Test
    void closingTwiceIsRejected() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.close();

        assertThatThrownBy(chat::close).isInstanceOf(InvalidChatOperationException.class);
    }

    @Test
    void cannotAddParticipantToClosedChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.close();

        assertThatThrownBy(() -> chat.addParticipant(userA, ParticipantRole.MEMBER))
                .isInstanceOf(ChatClosedException.class);
    }
}
