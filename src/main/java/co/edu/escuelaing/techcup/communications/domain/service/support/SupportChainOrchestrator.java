package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.AuditServiceClient;
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
    private static final String NOTIFICATION_TITLE = "Support ticket updated";

    private final SupportHandler supportChainHead;
    private final AuditServiceClient auditServiceClient;
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
        auditServiceClient.recordEvent(TRANSITION_EVENT, ticket.getId(), detail);
        // Best-effort: una falla notificando al usuario no debe tumbar la transición del
        // ticket, que ya quedó aplicada y auditada en las líneas de arriba.
        try {
            notificationServiceClient.notify(ticket.getRequesterId(), NOTIFICATION_TITLE, detail);
        } catch (IntegrationException ex) {
            log.warn("No fue posible notificar al usuario {} sobre el ticket {}: {}",
                    ticket.getRequesterId(), ticket.getId(), ex.getMessage());
        }
        return result;
    }
}
