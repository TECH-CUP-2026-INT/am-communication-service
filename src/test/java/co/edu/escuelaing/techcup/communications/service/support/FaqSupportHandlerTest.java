package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportOutcome;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqSupportHandlerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessagePublisher messagePublisher;

    @Mock
    private FaqRepository faqRepository;

    private FaqSupportHandler handler;

    private SupportTicket newTicket(String subject) {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        UUID requester = UUID.randomUUID();
        chat.addParticipant(requester, ParticipantRole.MEMBER);
        chat.addParticipant(SupportBotIdentity.BOT_USER_ID, ParticipantRole.MEMBER);
        return SupportTicket.open(chat, requester, subject);
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        handler = new FaqSupportHandler(messageRepository, messagePublisher, faqRepository);
    }

    @Test
    void onlyHandlesFaqLevel() {
        SupportTicket ticket = newTicket("cannot login");
        assertThat(handler.canHandle(ticket)).isTrue();

        ticket.escalateTo(SupportLevel.CHATBOT);
        assertThat(handler.canHandle(ticket)).isFalse();
    }

    @Test
    void answersWithTheMatchedFaqAndInvitesEscalationWithoutAdvancingTheLevel() {
        SupportTicket ticket = newTicket("I forgot my password and cannot login");
        Faq passwordFaq = Faq.create(Set.of("password", "login"),
                "You can reset your password from the login screen.");
        when(faqRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(passwordFaq));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportResult result = handler.handle(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.RESOLVED);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.FAQ);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).contains("reset your password").contains("Escalate");
        assertThat(captor.getValue().getSenderId()).isEqualTo(SupportBotIdentity.BOT_USER_ID);
        verify(messagePublisher).publishSupportMessage(ticket.getId(), captor.getValue());
    }

    @Test
    void answersWithAFallbackWhenNothingMatchesWithoutAdvancingTheLevel() {
        SupportTicket ticket = newTicket("my flying car won't start");
        Faq passwordFaq = Faq.create(Set.of("password", "login"), "Reset it from the login screen.");
        when(faqRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(passwordFaq));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportResult result = handler.handle(ticket);

        assertThat(result.outcome()).isEqualTo(SupportOutcome.RESOLVED);
        assertThat(ticket.getCurrentLevel()).isEqualTo(SupportLevel.FAQ);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).contains("couldn't find an answer").contains("Escalate");
    }

    @Test
    void firstMatchingFaqWinsWhenSeveralMatch() {
        SupportTicket ticket = newTicket("cannot login, also billing question");
        Faq loginFaq = Faq.create(Set.of("login"), "Login help.");
        Faq billingFaq = Faq.create(Set.of("billing"), "Billing help.");
        when(faqRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(loginFaq, billingFaq));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(ticket);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).contains("Login help.");
        assertThat(captor.getValue().getContent()).doesNotContain("Billing help.");
    }
}
