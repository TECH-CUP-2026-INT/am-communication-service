package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SupportTicketRepositoryTest {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TestEntityManager em;

    private final UUID requester = UUID.randomUUID();
    private final UUID stranger = UUID.randomUUID();

    @Test
    void recognisesOnlyTheParticipantsOfTheTicketChat() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(requester, ParticipantRole.MEMBER);
        chatRepository.save(chat);
        UUID ticketId = supportTicketRepository.save(SupportTicket.open(chat, requester, "cannot login")).getId();
        em.flush();
        em.clear();

        assertThat(supportTicketRepository.isParticipantOfTicketChat(ticketId, requester)).isTrue();
        assertThat(supportTicketRepository.isParticipantOfTicketChat(ticketId, stranger)).isFalse();
        assertThat(supportTicketRepository.isParticipantOfTicketChat(UUID.randomUUID(), requester)).isFalse();
    }
}
