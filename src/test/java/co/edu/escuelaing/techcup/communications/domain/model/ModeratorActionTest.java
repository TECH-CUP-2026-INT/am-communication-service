package co.edu.escuelaing.techcup.communications.domain.model;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModeratorActionTest {

    private final UUID moderator = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void recordsAction() {
        ModeratorAction action = ModeratorAction.record(
                moderator, "MESSAGE", target, ModeratorActionType.DELETE_MESSAGE, "abusive");

        assertThat(action.getId()).isNotNull();
        assertThat(action.getModeratorId()).isEqualTo(moderator);
        assertThat(action.getTargetType()).isEqualTo("MESSAGE");
        assertThat(action.getTargetId()).isEqualTo(target);
        assertThat(action.getActionType()).isEqualTo(ModeratorActionType.DELETE_MESSAGE);
        assertThat(action.getReason()).isEqualTo("abusive");
        assertThat(action.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsBlankTargetType() {
        assertThatThrownBy(() -> ModeratorAction.record(
                moderator, " ", target, ModeratorActionType.WARN, null))
                .isInstanceOf(InvalidChatOperationException.class);
    }
}
