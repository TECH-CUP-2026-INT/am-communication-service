package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.NotificationServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SupportChainOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SupportChainOrchestrator.class);

    private static final String TRANSITION_EVENT = "SUPPORT_TRANSITION";

    private final SupportHandler supportChainHead;
    private final NotificationServiceClient notificationServiceClient;


    public SupportResult runAutomatedStage(SupportTicket ticket) {
        SupportLevel from = ticket.getCurrentLevel();
        SupportResult result = supportChainHead.handle(ticket);
        return recordAndNotify(ticket, from, result);
    }


    public SupportResult escalate(SupportTicket ticket) {
        if (ticket.getCurrentLevel() == SupportLevel.FAQ) {
            SupportLevel from = ticket.getCurrentLevel();
            ticket.escalateTo(SupportLevel.CHATBOT);
            recordAndNotify(ticket, from, SupportResult.escalated(from, SupportLevel.CHATBOT));
        }
        return runAutomatedStage(ticket);
    }

    private SupportResult recordAndNotify(SupportTicket ticket, SupportLevel from, SupportResult result) {
        String detail = "%s: %s -> %s".formatted(result.outcome(), from, ticket.getCurrentLevel());
        // No audit service is deployed anywhere in the org (and none is planned), so the
        // transition is recorded locally instead of pretending there's somewhere to push it.
        log.info("{} ticket={} detail={}", TRANSITION_EVENT, ticket.getId(), detail);
        // Best-effort: a failure notifying the user must not tumble a transition that's already
        // applied to the ticket.
        try {
            notificationServiceClient.notify(ticket.getChatId(), ticket.getRequesterId(), detail);
        } catch (IntegrationException ex) {
            log.warn("No fue posible notificar al usuario {} sobre el ticket {}: {}",
                    ticket.getRequesterId(), ticket.getId(), ex.getMessage());
        }
        return result;
    }
}
