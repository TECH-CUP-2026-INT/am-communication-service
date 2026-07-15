package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatbotClient;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;
import org.springframework.stereotype.Component;


@Component
public class ChatbotSupportHandler extends AbstractAutomatedSupportHandler {

    private static final String FALLBACK_UNAVAILABLE_MESSAGE =
            "Nuestro asistente de IA no está disponible temporalmente; te estamos conectando con un moderador.";
    private static final int HISTORY_LIMIT = 10;

    private final ChatbotClient chatbotClient;

    public ChatbotSupportHandler(MessageRepository messageRepository, MessagePublisher messagePublisher,
                                  ChatbotClient chatbotClient) {
        super(messageRepository, messagePublisher);
        this.chatbotClient = chatbotClient;
    }

    @Override
    protected SupportLevel level() {
        return SupportLevel.CHATBOT;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        String prompt = new SupportPromptBuilder()
                .withSubject(ticket.getSubject())
                .withHistory(recentMessages(ticket, HISTORY_LIMIT))
                .build();
        String answer;
        try {
            answer = chatbotClient.generateReply(prompt);
        } catch (IntegrationException ex) {
            answer = FALLBACK_UNAVAILABLE_MESSAGE;
        }
        postBotMessage(ticket, answer);
        ticket.escalateTo(SupportLevel.MODERATOR);
        return SupportResult.escalated(SupportLevel.CHATBOT, SupportLevel.MODERATOR);
    }
}
