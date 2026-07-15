package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportTicketStatus;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.SupportTicketDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketJpaRepository extends JpaRepository<SupportTicketDao, UUID> {

    List<SupportTicketDao> findByRequesterId(UUID requesterId);

    List<SupportTicketDao> findByStatus(SupportTicketStatus status);

    /** A ticket is readable by whoever participates in the support chat it is attached to. */
    @Query("select count(t) > 0 from SupportTicketDao t where t.id = :ticketId and exists "
            + "(select 1 from ParticipantDao p where p.chat = t.chat and p.userId = :userId)")
    boolean isParticipantOfTicketChat(@Param("ticketId") UUID ticketId, @Param("userId") UUID userId);
}
