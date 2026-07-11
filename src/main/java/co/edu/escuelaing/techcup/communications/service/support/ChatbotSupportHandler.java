package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;

public class ChatbotSupportHandler extends AbstractSupportHandler {

    @Override
    protected SupportLevel level() {
        return SupportLevel.CHATBOT;
    }

    @Override
    protected SupportResult doHandle(SupportTicket ticket) {
        SupportLevel next = SupportLevel.AUTOMATIC;
        ticket.escalateTo(next);
        return SupportResult.escalated(SupportLevel.CHATBOT, next);
    }
}
