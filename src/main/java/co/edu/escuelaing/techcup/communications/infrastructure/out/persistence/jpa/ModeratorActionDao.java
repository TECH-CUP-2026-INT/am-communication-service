package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.domain.model.ModeratorAction;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Pure O/R mapping for the {@code moderator_actions} table. Holds no business logic or validation. */
@Entity
@Table(
        name = "moderator_actions",
        indexes = {
                @Index(name = "idx_action_moderator", columnList = "moderator_id"),
                @Index(name = "idx_action_target", columnList = "target_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ModeratorActionDao {

    public static final int MAX_REASON_LENGTH = ModeratorAction.MAX_REASON_LENGTH;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "moderator_id", nullable = false, updatable = false)
    private UUID moderatorId;

    @Column(name = "target_type", nullable = false, length = 30, updatable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false, updatable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, updatable = false)
    private ModeratorActionType actionType;

    @Column(length = MAX_REASON_LENGTH, updatable = false)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
