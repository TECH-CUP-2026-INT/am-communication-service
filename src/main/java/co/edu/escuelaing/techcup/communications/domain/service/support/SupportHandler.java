package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;

public interface SupportHandler {

    boolean canHandle(SupportTicket ticket);

    SupportResult handle(SupportTicket ticket);

    void setNext(SupportHandler next);
}
