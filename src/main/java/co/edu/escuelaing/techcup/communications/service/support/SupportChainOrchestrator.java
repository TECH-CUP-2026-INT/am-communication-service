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

    /**
     * Runs whichever handler matches the ticket's current level, in place. Used at ticket
     * creation (so the FAQ tier answers immediately) and internally by {@link #escalate} when
     * an explicit escalation lands on another automated tier.
     */
    public SupportResult runAutomatedStage(SupportTicket ticket) {
        SupportLevel from = ticket.getCurrentLevel();
        SupportResult result = supportChainHead.handle(ticket);
        return recordAndNotify(ticket, from, result);
    }

    /**
     * Explicit, user-triggered request to move the ticket forward. Every tier's handler already
     * advances the ticket unconditionally when invoked (chatbot, moderator, organizer) — FAQ is
     * the one exception, since it deliberately waits for the user instead of self-advancing. So
     * the only special case here is forcing the ticket past FAQ; every other level is handled by
     * simply re-running the chain, which reuses each handler's real logic (e.g. the organizer's
     * own {@code markPending()} call) instead of reimplementing the transition here.
     */
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
        auditServiceClient.record(TRANSITION_EVENT, ticket.getId(), detail);
        notificationServiceClient.notify(ticket.getRequesterId(), NOTIFICATION_TITLE, detail);
        return result;
    }
}
