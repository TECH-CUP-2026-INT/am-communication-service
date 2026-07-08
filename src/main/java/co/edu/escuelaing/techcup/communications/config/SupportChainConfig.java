package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.service.support.AutomaticSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.ChatbotSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.ModeratorSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.OrganizerSupportHandler;
import co.edu.escuelaing.techcup.communications.service.support.SupportHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the support Chain of Responsibility. Extending the chain is a matter
 * of inserting another handler in this single place.
 */
@Slf4j
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

    @Bean
    @ConditionalOnMissingBean(AuditServiceClient.class)
    public AuditServiceClient loggingAuditServiceClient() {
        return (eventType, entityId, detail) ->
                log.info("[AUDIT] type={} entityId={} detail={}", eventType, entityId, detail);
    }
}
