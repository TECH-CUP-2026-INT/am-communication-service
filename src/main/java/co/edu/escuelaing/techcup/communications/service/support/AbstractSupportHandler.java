package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;

/**
 * Template for a link in the support chain. A handler is responsible for a single
 * {@link SupportLevel}; when it is not responsible for the ticket it delegates to
 * the next handler, keeping the chain free of if/else ladders and easy to extend.
 */
public abstract class AbstractSupportHandler implements SupportHandler {

    private SupportHandler next;

    @Override
    public void setNext(SupportHandler next) {
        this.next = next;
    }

    @Override
    public boolean canHandle(SupportTicket ticket) {
        return ticket.getCurrentLevel() == level();
    }

    @Override
    public SupportResult handle(SupportTicket ticket) {
        if (canHandle(ticket)) {
            return doHandle(ticket);
        }
        if (next != null) {
            return next.handle(ticket);
        }
        return SupportResult.pending(ticket.getCurrentLevel());
    }

    protected abstract SupportLevel level();

    protected abstract SupportResult doHandle(SupportTicket ticket);
}
