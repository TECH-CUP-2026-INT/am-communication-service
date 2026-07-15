package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.MessageDao;
import org.mapstruct.Mapper;

/**
 * Maps {@link MessageDao} to/from {@link Message}. The {@code chat} association is mapped as a
 * lightweight {@link Chat#reference(java.util.UUID)} stub (id only): no caller ever traverses a
 * Message's chat beyond {@code getChatId()}, so the full aggregate is never hydrated here -
 * doing so would also defeat the point of the {@code chat_id} being a LAZY proxy.
 */
@Mapper(componentModel = "spring")
public interface MessagePersistenceMapper {

    default Message toDomain(MessageDao dao) {
        return Message.fromPersistence(dao.getId(), Chat.reference(dao.getChat().getId()), dao.getSenderId(),
                dao.getContent(), dao.getStatus(), dao.getSentAt());
    }

    default MessageDao toDao(Message message) {
        MessageDao dao = new MessageDao();
        dao.setId(message.getId());
        dao.setSenderId(message.getSenderId());
        dao.setContent(message.getContent());
        dao.setStatus(message.getStatus());
        dao.setSentAt(message.getSentAt());
        return dao;
    }
}
