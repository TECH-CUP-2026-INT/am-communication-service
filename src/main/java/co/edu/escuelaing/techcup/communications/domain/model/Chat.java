package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatStatus;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.ParticipantNotAllowedException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Chat {

    private UUID id;
    private ChatType type;
    private UUID teamId;
    private ChatStatus status;
    private Set<Participant> participants = new HashSet<>();
    private Instant createdAt;

    private Chat(ChatType type, UUID teamId) {
        this.id = UUID.randomUUID();
        this.type = Objects.requireNonNull(type, "type is required");
        this.teamId = teamId;
        this.status = ChatStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public static Chat create(ChatType type, UUID teamId) {
        Objects.requireNonNull(type, "type is required");
        if (type == ChatType.GROUP && teamId == null) {
            throw new InvalidChatOperationException("Un chat de tipo GROUP requiere un teamId");
        }
        if (type != ChatType.GROUP && teamId != null) {
            throw new InvalidChatOperationException("Solo los chats de tipo GROUP pueden tener un teamId");
        }
        return new Chat(type, teamId);
    }

    public static Chat fromPersistence(UUID id, ChatType type, UUID teamId, ChatStatus status, Instant createdAt) {
        Chat chat = new Chat();
        chat.id = id;
        chat.type = type;
        chat.teamId = teamId;
        chat.status = status;
        chat.createdAt = createdAt;
        return chat;
    }

    /**
     * Lightweight stub carrying only the id, for cross-aggregate references that never need
     * more than {@code getId()} (see {@code Message.chat}). Persistence mappers only.
     */
    public static Chat reference(UUID id) {
        Chat chat = new Chat();
        chat.id = id;
        return chat;
    }

    public void attachParticipant(Participant participant) {
        this.participants.add(participant);
    }

    public Participant addParticipant(UUID userId, ParticipantRole role) {
        ensureOpen();
        if (isParticipant(userId)) {
            throw new InvalidChatOperationException("El usuario " + userId + " ya es participante del chat " + id);
        }
        Participant participant = new Participant(this, userId, role);
        participants.add(participant);
        return participant;
    }

    public boolean isParticipant(UUID userId) {
        return participants.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public Message postMessage(UUID senderId, String content) {
        ensureOpen();
        if (!isParticipant(senderId)) {
            throw new ParticipantNotAllowedException(senderId, id);
        }
        return new Message(this, senderId, content);
    }

    public void close() {
        if (status == ChatStatus.CLOSED) {
            throw new InvalidChatOperationException("El chat ya está cerrado: " + id);
        }
        this.status = ChatStatus.CLOSED;
    }

    public boolean isClosed() {
        return status == ChatStatus.CLOSED;
    }

    public Set<Participant> getParticipants() {
        return Collections.unmodifiableSet(participants);
    }

    private void ensureOpen() {
        if (status == ChatStatus.CLOSED) {
            throw new ChatClosedException(id);
        }
    }
}
