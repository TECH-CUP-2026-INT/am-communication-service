package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Pure O/R mapping for the {@code participants} table. Holds no business logic or validation. */
@Entity
@Table(
        name = "participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_participant_chat_user", columnNames = {"chat_id", "user_id"}),
        indexes = @Index(name = "idx_participant_user", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ParticipantDao {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatDao chat;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;
}
