package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;

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
