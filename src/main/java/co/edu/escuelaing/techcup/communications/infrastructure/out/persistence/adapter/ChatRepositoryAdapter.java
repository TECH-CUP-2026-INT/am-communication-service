package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.ChatPersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ChatJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ChatRepositoryAdapter implements ChatRepository {

    private final ChatJpaRepository jpaRepository;
    private final ChatPersistenceMapper mapper;

    @Override
    public Optional<Chat> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Chat> findByTeamIdAndType(UUID teamId, ChatType type) {
        return jpaRepository.findByTeamIdAndType(teamId, type).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Chat save(Chat chat) {
        return mapper.toDomain(jpaRepository.save(mapper.toDao(chat)));
    }

    @Override
    public List<Chat> findAllByParticipantUserId(UUID userId) {
        return jpaRepository.findAllByParticipantUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean isParticipant(UUID chatId, UUID userId) {
        return jpaRepository.isParticipant(chatId, userId);
    }
}
