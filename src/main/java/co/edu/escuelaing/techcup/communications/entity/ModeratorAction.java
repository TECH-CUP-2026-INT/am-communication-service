package co.edu.escuelaing.techcup.communications.entity;

import co.edu.escuelaing.techcup.communications.entity.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "moderator_actions",
        indexes = {
                @Index(name = "idx_action_moderator", columnList = "moderator_id"),
                @Index(name = "idx_action_target", columnList = "target_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ModeratorAction {

    public static final int MAX_REASON_LENGTH = 500;

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

    private ModeratorAction(UUID moderatorId, String targetType, UUID targetId,
                            ModeratorActionType actionType, String reason) {
        this.id = UUID.randomUUID();
        this.moderatorId = Objects.requireNonNull(moderatorId, "moderatorId is required");
        this.targetType = requireText(targetType, "targetType");
        this.targetId = Objects.requireNonNull(targetId, "targetId is required");
        this.actionType = Objects.requireNonNull(actionType, "actionType is required");
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public static ModeratorAction record(UUID moderatorId, String targetType, UUID targetId,
                                         ModeratorActionType actionType, String reason) {
        return new ModeratorAction(moderatorId, targetType, targetId, actionType, reason);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidChatOperationException(field + " must not be blank");
        }
        return value;
    }
}
