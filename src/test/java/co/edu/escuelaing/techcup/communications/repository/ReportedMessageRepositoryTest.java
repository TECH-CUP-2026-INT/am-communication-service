package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportedMessageRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReportedMessageRepository reportedMessageRepository;

    @Autowired
    private TestEntityManager em;

    private final UUID sender = UUID.randomUUID();
    private final UUID reporter = UUID.randomUUID();

    private Message persistedMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        chatRepository.save(chat);
        return messageRepository.save(chat.postMessage(sender, "reported text"));
    }

    @Test
    void detectsExistingReportByMessageAndReporter() {
        Message message = persistedMessage();
        reportedMessageRepository.save(ReportedMessage.create(message, reporter, "spam"));
        em.flush();
        em.clear();

        assertThat(reportedMessageRepository.existsByMessage_IdAndReporterId(message.getId(), reporter)).isTrue();
        assertThat(reportedMessageRepository.existsByMessage_IdAndReporterId(message.getId(), UUID.randomUUID())).isFalse();
    }

    @Test
    void findsPendingReports() {
        Message message = persistedMessage();
        reportedMessageRepository.save(ReportedMessage.create(message, reporter, "spam"));
        em.flush();
        em.clear();

        assertThat(reportedMessageRepository.findByStatus(ReportStatus.PENDING)).hasSize(1);
        assertThat(reportedMessageRepository.findByStatus(ReportStatus.ACTIONED)).isEmpty();
    }
}
