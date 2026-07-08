package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;

public class AutomaticSupportHandler extends AbstractSupportHandler {

    @Override
    protected SupportLevel level() {
        return SupportLevel.AUTOMATIC;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        SupportLevel next = SupportLevel.MODERATOR;
        ticket.escalateTo(next);
        return SupportResult.escalated(SupportLevel.AUTOMATIC, next);
    }
}
