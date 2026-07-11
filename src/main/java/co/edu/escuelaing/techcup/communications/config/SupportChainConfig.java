package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.service.support.AutomaticSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.ChatbotSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.ModeratorSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.OrganizerSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.SupportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the support Chain of Responsibility. Extending the chain is a matter
 * of inserting another handler in this single place.
 */
@Configuration
public class SupportChainConfig {

    @Bean
    public SupportHandler supportChainHead() {
        SupportHandler chatbot = new ChatbotSupportHandler();
        SupportHandler automatic = new AutomaticSupportHandler();
        SupportHandler moderator = new ModeratorSupportHandler();
        SupportHandler organizer = new OrganizerSupportHandler();

        chatbot.setNext(automatic);
        automatic.setNext(moderator);
        moderator.setNext(organizer);

        return chatbot;
    }
}
