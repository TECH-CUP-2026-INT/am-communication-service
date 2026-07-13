package co.edu.escuelaing.techcup.communications.service.client;

/** Outbound port to an AI chat completion service used by the chatbot support tier. */
public interface ChatbotClient {

    String generateReply(String userMessage);
}
