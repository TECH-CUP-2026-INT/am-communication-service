package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.service.client.AuditServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Runs the support Chain of Responsibility and audits every transition.
 */
@Component
@RequiredArgsConstructor
public class SupportChainOrchestrator {

    private final SupportHandler supportChainHead;
    private final AuditServiceClient auditServiceClient;

    public SupportResult process(SupportTicket ticket) {
        SupportLevel from = ticket.getCurrentLevel();
        SupportResult result = supportChainHead.handle(ticket);
        auditServiceClient.record(
                "SUPPORT_TRANSITION",
                ticket.getId(),
                "%s: %s -> %s".formatted(result.outcome(), from, ticket.getCurrentLevel()));
        return result;
    }
}
