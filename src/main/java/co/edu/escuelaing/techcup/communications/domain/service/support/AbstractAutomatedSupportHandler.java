package co.edu.escuelaing.techcup.communications.domain.service.support;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessagePublisher;

import java.util.List;


public abstract class AbstractAutomatedSupportHandler extends AbstractSupportHandler {

    private final MessageRepository messageRepository;
    private final MessagePublisher messagePublisher;

    protected AbstractAutomatedSupportHandler(MessageRepository messageRepository, MessagePublisher messagePublisher) {
        this.messageRepository = messageRepository;
        this.messagePublisher = messagePublisher;
    }

    protected Message postBotMessage(SupportTicket ticket, String content) {
        String trimmed = content.length() > Message.MAX_CONTENT_LENGTH
                ? content.substring(0, Message.MAX_CONTENT_LENGTH)
                : content;
        Message saved = messageRepository.save(ticket.getChat().postMessage(SupportBotIdentity.BOT_USER_ID, trimmed));
        messagePublisher.publishSupportMessage(ticket.getId(), saved);
        return saved;
    }

    /** @return up to the last {@code limit} messages of the ticket's chat, oldest first. */
    protected List<Message> recentMessages(SupportTicket ticket, int limit) {
        List<Message> all = messageRepository.findByChat_IdOrderBySentAtAsc(ticket.getChatId());
        int from = Math.max(0, all.size() - limit);
        return all.subList(from, all.size());
    }
}
