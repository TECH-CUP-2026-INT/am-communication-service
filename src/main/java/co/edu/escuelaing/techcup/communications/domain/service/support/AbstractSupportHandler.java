package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;


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
