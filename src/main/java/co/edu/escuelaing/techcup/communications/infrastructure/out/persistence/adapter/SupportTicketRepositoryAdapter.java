package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.ChatPersistenceMapper;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.SupportTicketPersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.SupportTicketDao;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ChatJpaRepository;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.SupportTicketJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class SupportTicketRepositoryAdapter implements SupportTicketRepository {

    private final SupportTicketJpaRepository jpaRepository;
    private final ChatJpaRepository chatJpaRepository;
    private final SupportTicketPersistenceMapper mapper;
    private final ChatPersistenceMapper chatMapper;

    @Override
    public Optional<SupportTicket> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public SupportTicket save(SupportTicket ticket) {
        SupportTicketDao dao = mapper.toDao(ticket);
        dao.setChat(chatJpaRepository.getReferenceById(ticket.getChatId()));
        return toDomain(jpaRepository.save(dao));
    }

    @Override
    public boolean isParticipantOfTicketChat(UUID ticketId, UUID userId) {
        return jpaRepository.isParticipantOfTicketChat(ticketId, userId);
    }

    /** {@code chat} must be fully hydrated (participants included) - see {@link SupportTicketPersistenceMapper}. */
    private SupportTicket toDomain(SupportTicketDao dao) {
        Chat chat = chatMapper.toDomain(dao.getChat());
        return mapper.toDomain(dao, chat);
    }
}
