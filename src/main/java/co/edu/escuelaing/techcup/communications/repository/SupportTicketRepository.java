package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    List<SupportTicket> findByRequesterId(UUID requesterId);

    List<SupportTicket> findByStatus(SupportTicketStatus status);

    /** A ticket is readable by whoever participates in the support chat it is attached to. */
    @Query("select count(t) > 0 from SupportTicket t where t.id = :ticketId and exists "
            + "(select 1 from Participant p where p.chat = t.chat and p.userId = :userId)")
    boolean isParticipantOfTicketChat(@Param("ticketId") UUID ticketId, @Param("userId") UUID userId);
}
