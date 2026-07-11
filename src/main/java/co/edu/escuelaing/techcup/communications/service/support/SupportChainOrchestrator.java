package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import co.edu.escuelaing.techcup.communications.service.client.NotificationServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Runs the support Chain of Responsibility, audits every transition and tells the
 * requester where the ticket stands. The handlers stay pure: only this orchestrator
 * reaches the outbound ports.
 */
@Component
@RequiredArgsConstructor
public class SupportChainOrchestrator {

    private static final String TRANSITION_EVENT = "SUPPORT_TRANSITION";
    private static final String NOTIFICATION_TITLE = "Support ticket updated";

    private final SupportHandler supportChainHead;
    private final AuditServiceClient auditServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public SupportResult process(SupportTicket ticket) {
        SupportLevel from = ticket.getCurrentLevel();
        SupportResult result = supportChainHead.handle(ticket);
        String detail = "%s: %s -> %s".formatted(result.outcome(), from, ticket.getCurrentLevel());

        auditServiceClient.record(TRANSITION_EVENT, ticket.getId(), detail);
        notificationServiceClient.notify(ticket.getRequesterId(), NOTIFICATION_TITLE, detail);
        return result;
    }
}
