package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

/** Outbound port to an AI chat completion service used by the chatbot support tier. */
public interface ChatbotClient {

    String generateReply(String userMessage);
}
