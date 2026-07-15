package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Participant {

    private UUID id;
    private Chat chat;
    private UUID userId;
    private ParticipantRole role;
    private Instant joinedAt;

    Participant(Chat chat, UUID userId, ParticipantRole role) {
        this.id = UUID.randomUUID();
        this.chat = Objects.requireNonNull(chat, "chat is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.role = Objects.requireNonNull(role, "role is required");
        this.joinedAt = Instant.now();
    }

    public static Participant fromPersistence(UUID id, Chat chat, UUID userId, ParticipantRole role, Instant joinedAt) {
        Participant participant = new Participant();
        participant.id = id;
        participant.chat = chat;
        participant.userId = userId;
        participant.role = role;
        participant.joinedAt = joinedAt;
        return participant;
    }
}
