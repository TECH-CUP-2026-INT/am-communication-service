package co.edu.escuelaing.techcup.communications.config;

import co.edu.escuelaing.techcup.communications.exception.SubscriptionNotAllowedException;
import co.edu.escuelaing.techcup.communications.service.ChatAccessUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Guards the topics a STOMP session may listen to. Sending is already restricted by the domain,
 * which refuses a message from a non-participant; without this guard any authenticated session
 * could still subscribe to a stranger's conversation and read it.
 */
@Component
public class SubscriptionAuthorizer {

    /** A topic prefix and the access question its trailing identifier answers. */
    private record TopicRule(String prefix, BiPredicate<UUID, UUID> grantsAccess) {
    }

    private final List<TopicRule> rules;

    public SubscriptionAuthorizer(ChatAccessUseCase chatAccess) {
        this.rules = List.of(
                new TopicRule(WebSocketMessagePublisher.CHAT_TOPIC, chatAccess::canReadChat),
                new TopicRule(WebSocketMessagePublisher.SUPPORT_TOPIC, chatAccess::canReadSupportTicket));
    }

    public void authorize(String destination, AuthenticatedUser subscriber) {
        TopicRule rule = ruleFor(destination);
        if (!rule.grantsAccess().test(targetIdOf(destination, rule), subscriber.userId())) {
            throw new SubscriptionNotAllowedException(subscriber.userId(), destination);
        }
    }

    private TopicRule ruleFor(String destination) {
        return rules.stream()
                .filter(rule -> destination != null && destination.startsWith(rule.prefix()))
                .findFirst()
                .orElseThrow(() -> new SubscriptionNotAllowedException(destination));
    }

    private UUID targetIdOf(String destination, TopicRule rule) {
        try {
            return UUID.fromString(destination.substring(rule.prefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new SubscriptionNotAllowedException(destination);
        }
    }
}
