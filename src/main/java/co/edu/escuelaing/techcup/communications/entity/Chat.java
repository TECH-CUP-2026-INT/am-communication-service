package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ChatStatus;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Chat {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private ChatType type;

    @Column(name = "team_id", updatable = false)
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatStatus status;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participant> participants = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
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
            throw new InvalidChatOperationException("A GROUP chat requires a teamId");
        }
        if (type != ChatType.GROUP && teamId != null) {
            throw new InvalidChatOperationException("Only GROUP chats may reference a teamId");
        }
        return new Chat(type, teamId);
    }

    public Participant addParticipant(UUID userId, ParticipantRole role) {
        ensureOpen();
        if (isParticipant(userId)) {
            throw new InvalidChatOperationException("User " + userId + " is already a participant of chat " + id);
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
            throw new InvalidChatOperationException("Chat is already closed: " + id);
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
