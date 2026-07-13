package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.repository.MessageRepository;
import co.edu.escuelaing.techcup.communications.service.client.ChatbotClient;
import co.edu.escuelaing.techcup.communications.service.client.MessagePublisher;
import org.springframework.stereotype.Component;

/**
 * Second support tier: a real AI assistant (Groq). Always escalates to a human moderator right
 * after answering, whether or not the answer resolves the user's issue — this tier never leaves
 * a ticket waiting on its own.
 */
@Component
public class ChatbotSupportHandler extends AbstractAutomatedSupportHandler {

    private static final String FALLBACK_UNAVAILABLE_MESSAGE =
            "Our AI assistant is temporarily unavailable; connecting you with a moderator.";
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
