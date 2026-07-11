package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager em;

    private final UUID sender = UUID.randomUUID();

    private Chat persistedChatWithSender() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chatRepository.save(chat);
    }

    @Test
    void findsAllMessagesOfChat() {
        Chat chat = persistedChatWithSender();
        messageRepository.save(chat.postMessage(sender, "one"));
        messageRepository.save(chat.postMessage(sender, "two"));
        messageRepository.save(chat.postMessage(sender, "three"));
        em.flush();
        em.clear();

        List<Message> messages = messageRepository.findByChat_IdOrderBySentAtAsc(chat.getId());

        assertThat(messages).hasSize(3);
        assertThat(messages).allMatch(m -> m.getChatId().equals(chat.getId()));
    }

    @Test
    void paginatesMessagesOfChat() {
        Chat chat = persistedChatWithSender();
        for (int i = 0; i < 3; i++) {
            messageRepository.save(chat.postMessage(sender, "msg-" + i));
        }
        em.flush();
        em.clear();

        Page<Message> firstPage = messageRepository.findByChat_Id(chat.getId(), PageRequest.of(0, 2));

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
