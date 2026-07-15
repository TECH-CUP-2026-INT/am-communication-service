package co.edu.escuelaing.techcup.communications.infrastructure.config;

import co.edu.escuelaing.techcup.communications.domain.service.support.ChatbotSupportHandler;
import co.edu.escuelaing.techcup.communications.domain.service.support.FaqSupportHandler;
import co.edu.escuelaing.techcup.communications.domain.service.support.ModeratorSupportHandler;
import co.edu.escuelaing.techcup.communications.domain.service.support.OrganizerSupportHandler;
import co.edu.escuelaing.techcup.communications.domain.service.support.SupportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the support Chain of Responsibility. Extending the chain is a matter
 * of inserting another handler in this single place.
 */
@Configuration
public class SupportChainConfig {

    @Bean
    public SupportHandler supportChainHead(FaqSupportHandler faq, ChatbotSupportHandler chatbot) {
        SupportHandler moderator = new ModeratorSupportHandler();
        SupportHandler organizer = new OrganizerSupportHandler();

        faq.setNext(chatbot);
        chatbot.setNext(moderator);
        moderator.setNext(organizer);

        return faq;
    }
}
