package co.edu.escuelaing.techcup.communications.entity.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupportLevelTest {

    @Test
    void followsEscalationOrder() {
        assertThat(SupportLevel.CHATBOT.next()).isEqualTo(SupportLevel.AUTOMATIC);
        assertThat(SupportLevel.AUTOMATIC.next()).isEqualTo(SupportLevel.MODERATOR);
        assertThat(SupportLevel.MODERATOR.next()).isEqualTo(SupportLevel.ORGANIZER);
        assertThat(SupportLevel.ORGANIZER.next()).isEqualTo(SupportLevel.PENDING);
    }

    @Test
    void pendingIsTerminal() {
        assertThat(SupportLevel.PENDING.next()).isEqualTo(SupportLevel.PENDING);
        assertThat(SupportLevel.PENDING.isTerminal()).isTrue();
        assertThat(SupportLevel.CHATBOT.isTerminal()).isFalse();
    }
}
