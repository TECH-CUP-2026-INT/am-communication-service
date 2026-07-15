package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Participant;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ChatDao;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ParticipantDao;
import org.mapstruct.Mapper;

/**
 * Maps {@link ChatDao} (pure O/R mapping) to/from {@link Chat} (domain, with business logic).
 * Chat's public API has no setters - it only exposes factories that protect its invariants -
 * so the mapping is hand-written via the persistence-only reconstitution factories rather than
 * MapStruct's reflective field mapping.
 */
@Mapper(componentModel = "spring")
public interface ChatPersistenceMapper {

    default Chat toDomain(ChatDao dao) {
        Chat chat = Chat.fromPersistence(dao.getId(), dao.getType(), dao.getTeamId(), dao.getStatus(), dao.getCreatedAt());
        dao.getParticipants().forEach(participantDao -> chat.attachParticipant(
                Participant.fromPersistence(participantDao.getId(), chat, participantDao.getUserId(),
                        participantDao.getRole(), participantDao.getJoinedAt())));
        return chat;
    }

    default ChatDao toDao(Chat chat) {
        ChatDao dao = new ChatDao();
        dao.setId(chat.getId());
        dao.setType(chat.getType());
        dao.setTeamId(chat.getTeamId());
        dao.setStatus(chat.getStatus());
        dao.setCreatedAt(chat.getCreatedAt());
        chat.getParticipants().forEach(participant -> {
            ParticipantDao participantDao = new ParticipantDao();
            participantDao.setId(participant.getId());
            participantDao.setChat(dao);
            participantDao.setUserId(participant.getUserId());
            participantDao.setRole(participant.getRole());
            participantDao.setJoinedAt(participant.getJoinedAt());
            dao.attachParticipant(participantDao);
        });
        return dao;
    }
}
