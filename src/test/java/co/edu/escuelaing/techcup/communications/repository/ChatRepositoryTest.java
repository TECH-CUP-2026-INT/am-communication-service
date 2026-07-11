package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TestEntityManager em;

    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();

    @Test
    void persistsChatWithParticipantsAndReloads() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);
        chat.addParticipant(userB, ParticipantRole.MEMBER);

        UUID id = chatRepository.save(chat).getId();
        em.flush();
        em.clear();

        Chat reloaded = chatRepository.findById(id).orElseThrow();
        assertThat(reloaded.getParticipants()).hasSize(2);
        assertThat(reloaded.isParticipant(userA)).isTrue();
        assertThat(reloaded.isParticipant(userB)).isTrue();
    }

    @Test
    void findsDistinctChatsByParticipantUser() {
        Chat shared = Chat.create(ChatType.DIRECT, null);
        shared.addParticipant(userA, ParticipantRole.MEMBER);
        shared.addParticipant(userB, ParticipantRole.MEMBER);

        Chat onlyA = Chat.create(ChatType.GROUP, UUID.randomUUID());
        onlyA.addParticipant(userA, ParticipantRole.MEMBER);

        chatRepository.save(shared);
        chatRepository.save(onlyA);
        em.flush();
        em.clear();

        List<Chat> chatsOfA = chatRepository.findDistinctByParticipants_UserId(userA);
        List<Chat> chatsOfB = chatRepository.findDistinctByParticipants_UserId(userB);

        assertThat(chatsOfA).hasSize(2);
        assertThat(chatsOfB).hasSize(1);
    }
}
