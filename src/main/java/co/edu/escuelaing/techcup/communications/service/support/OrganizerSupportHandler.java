package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;

/**
 * Last human level in the chain. When the organizer cannot resolve the ticket it
 * is finalized as PENDING, awaiting a manual decision.
 */
public class OrganizerSupportHandler extends AbstractSupportHandler {

    @Override
    protected SupportLevel level() {
        return SupportLevel.ORGANIZER;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        ticket.markPending();
        return SupportResult.finalized(SupportLevel.ORGANIZER, SupportLevel.PENDING);
    }
}
