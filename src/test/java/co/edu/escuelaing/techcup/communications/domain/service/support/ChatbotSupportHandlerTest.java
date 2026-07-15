package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportOutcome;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatbotClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotSupportHandlerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessagePublisher messagePublisher;

    @Mock
    private ChatbotClient chatbotClient;

    private ChatbotSupportHandler handler;

    private SupportTicket newTicketAtChatbotLevel(String subject) {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        UUID requester = UUID.randomUUID();
        chat.addParticipant(requester, ParticipantRole.MEMBER);
        chat.addParticipant(SupportBotIdentity.BOT_USER_ID, ParticipantRole.MEMBER);
        SupportTicket ticket = SupportTicket.open(chat, requester, subject);
        ticket.escalateTo(SupportLevel.CHATBOT);
        return ticket;
    }

    @BeforeEach
    void setUp() {
        handler = new ChatbotSupportHandler(messageRepository, messagePublisher, chatbotClient);
    }

    @Test
    void answersWithTheAiReplyAndAlwaysEscalatesToModerator() {
        SupportTicket ticket = newTicketAtChatbotLevel("I can't log in");
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(ticket.getChatId())).thenReturn(List.of());
        when(chatbotClient.generateReply("Ticket subject: I can't log in")).thenReturn("Try clearing your browser cache.");

        SupportResult result = handler.handle(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.ESCALATED);
        assertThat(result.from()).isEqualTo(SupportLevel.CHATBOT);
        assertThat(result.to()).isEqualTo(SupportLevel.MODERATOR);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.MODERATOR);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Try clearing your browser cache.");
        verify(messagePublisher).publishSupportMessage(ticket.getId(), captor.getValue());
    }

    @Test
    void includesPriorChatMessagesInThePromptSentToTheAi() {
        SupportTicket ticket = newTicketAtChatbotLevel("cannot join tournament");
        Message faqReply = ticket.getChat().postMessage(SupportBotIdentity.BOT_USER_ID, "Try the Teams section.");
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(ticket.getChatId())).thenReturn(List.of(faqReply));
        when(chatbotClient.generateReply(any())).thenReturn("Let's dig deeper into that.");

        handler.handle(ticket);

        verify(chatbotClient).generateReply(eq("""
                Ticket subject: cannot join tournament

                Conversation so far:
                - Assistant: Try the Teams section."""));
    }

    @Test
    void fallsBackAndStillEscalatesWhenGroqIsUnavailable() {
        SupportTicket ticket = newTicketAtChatbotLevel("I can't log in");
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(ticket.getChatId())).thenReturn(List.of());
        when(chatbotClient.generateReply(any())).thenThrow(new IntegrationException("groq chatbot service", new RuntimeException()));

        SupportResult result = handler.handle(ticket);

        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.MODERATOR);
        assertThat(result.outcome()).isEqualTo(SupportOutcome.ESCALATED);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).contains("no está disponible temporalmente");
    }

    @Test
    void truncatesRepliesLongerThanTheMessageLimit() {
        SupportTicket ticket = newTicketAtChatbotLevel("issue");
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(ticket.getChatId())).thenReturn(List.of());
        String longReply = "a".repeat(Message.MAX_CONTENT_LENGTH + 50);
        when(chatbotClient.generateReply(any())).thenReturn(longReply);

        handler.handle(ticket);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).hasSize(Message.MAX_CONTENT_LENGTH);
    }

    @Test
    void onlyHandlesChatbotLevel() {
        SupportTicket ticket = newTicketAtChatbotLevel("issue");
        assertThat(handler.canHandle(ticket)).isTrue();

        ticket.escalateTo(SupportLevel.MODERATOR);
        assertThat(handler.canHandle(ticket)).isFalse();
    }
}
