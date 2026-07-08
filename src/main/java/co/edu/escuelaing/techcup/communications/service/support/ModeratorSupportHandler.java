package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;

public class ModeratorSupportHandler extends AbstractSupportHandler {

    @Override
    protected SupportLevel level() {
        return SupportLevel.MODERATOR;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        SupportLevel next = SupportLevel.ORGANIZER;
        ticket.escalateTo(next);
        return SupportResult.escalated(SupportLevel.MODERATOR, next);
    }
}
