package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
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
public class ModeratorAction {

    public static final int MAX_REASON_LENGTH = 500;

    private UUID id;
    private UUID moderatorId;
    private String targetType;
    private UUID targetId;
    private ModeratorActionType actionType;
    private String reason;
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

    public static ModeratorAction fromPersistence(UUID id, UUID moderatorId, String targetType, UUID targetId,
                                                  ModeratorActionType actionType, String reason, Instant createdAt) {
        ModeratorAction action = new ModeratorAction();
        action.id = id;
        action.moderatorId = moderatorId;
        action.targetType = targetType;
        action.targetId = targetId;
        action.actionType = actionType;
        action.reason = reason;
        action.createdAt = createdAt;
        return action;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidChatOperationException(field + " no puede estar vacío");
        }
        return value;
    }
}
