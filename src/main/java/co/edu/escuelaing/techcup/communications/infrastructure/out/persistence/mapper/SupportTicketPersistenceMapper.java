package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.SupportTicketDao;
import org.mapstruct.Mapper;

/**
 * Maps {@link SupportTicketDao} to/from {@link SupportTicket}. The {@code chat} association must
 * be the fully hydrated {@link Chat} (participants included, not a reference stub) because
 * {@code ReplySupportTicketService} and the automated support handlers call
 * {@code getChat().isParticipant()/addParticipant()/postMessage()} and save the chat back - a
 * stub would silently drop every existing participant on save (orphanRemoval). Hydration is
 * composed by {@code SupportTicketRepositoryAdapter} and passed in, keeping this mapper stateless.
 */
@Mapper(componentModel = "spring")
public interface SupportTicketPersistenceMapper {

    default SupportTicket toDomain(SupportTicketDao dao, Chat hydratedChat) {
        return SupportTicket.fromPersistence(dao.getId(), hydratedChat, dao.getRequesterId(), dao.getSubject(),
                dao.getStatus(), dao.getCurrentLevel(), dao.getAssignedTo(), dao.getCreatedAt(), dao.getUpdatedAt());
    }

    default SupportTicketDao toDao(SupportTicket ticket) {
        SupportTicketDao dao = new SupportTicketDao();
        dao.setId(ticket.getId());
        dao.setRequesterId(ticket.getRequesterId());
        dao.setSubject(ticket.getSubject());
        dao.setStatus(ticket.getStatus());
        dao.setCurrentLevel(ticket.getCurrentLevel());
        dao.setAssignedTo(ticket.getAssignedTo());
        dao.setCreatedAt(ticket.getCreatedAt());
        dao.setUpdatedAt(ticket.getUpdatedAt());
        return dao;
    }
}
