package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.MessagePersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.MessageDao;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ChatJpaRepository;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.MessageJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository jpaRepository;
    private final ChatJpaRepository chatJpaRepository;
    private final MessagePersistenceMapper mapper;

    @Override
    public Optional<Message> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Message save(Message message) {
        MessageDao dao = mapper.toDao(message);
        dao.setChat(chatJpaRepository.getReferenceById(message.getChatId()));
        return mapper.toDomain(jpaRepository.save(dao));
    }

    @Override
    public Page<Message> findByChat_Id(UUID chatId, Pageable pageable) {
        return jpaRepository.findByChat_Id(chatId, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Message> findByChat_IdOrderBySentAtAsc(UUID chatId) {
        return jpaRepository.findByChat_IdOrderBySentAtAsc(chatId).stream().map(mapper::toDomain).toList();
    }
}
