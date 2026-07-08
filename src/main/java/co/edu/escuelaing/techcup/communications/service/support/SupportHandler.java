package co.edu.escuelaing.techcup.communications.service.support;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;

public interface SupportHandler {

    boolean canHandle(SupportTicket ticket);

    SupportResult handle(SupportTicket ticket);

    void setNext(SupportHandler next);
}
