package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SupportPromptBuilderTest {

    private Chat chatWithParticipants() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(SupportBotIdentity.BOT_USER_ID, ParticipantRole.MEMBER);
        chat.addParticipant(UUID.randomUUID(), ParticipantRole.MEMBER);
        return chat;
    }

    @Test
    void includesOnlyTheSubjectWhenThereIsNoHistory() {
        String prompt = new SupportPromptBuilder()
                .withSubject("cannot login")
                .build();

        assertThat(prompt).isEqualTo("Ticket subject: cannot login");
    }

    @Test
    void appendsHistoryLabelingBotAndUserMessagesByRole() {
        Chat chat = chatWithParticipants();
        UUID requester = chat.getParticipants().stream()
                .map(p -> p.getUserId())
                .filter(id -> !id.equals(SupportBotIdentity.BOT_USER_ID))
                .findFirst().orElseThrow();

        Message botMessage = chat.postMessage(SupportBotIdentity.BOT_USER_ID, "You can reset your password.");
        Message userMessage = chat.postMessage(requester, "That didn't work for me.");

        String prompt = new SupportPromptBuilder()
                .withSubject("cannot login")
                .withHistory(List.of(botMessage, userMessage))
                .build();

        assertThat(prompt).isEqualTo("""
                Ticket subject: cannot login

                Conversation so far:
                - Assistant: You can reset your password.
                - User: That didn't work for me.""");
    }

    @Test
    void treatsAnEmptyHistoryListTheSameAsNoHistory() {
        String prompt = new SupportPromptBuilder()
                .withSubject("cannot login")
                .withHistory(List.of())
                .build();

        assertThat(prompt).isEqualTo("Ticket subject: cannot login");
    }
}
