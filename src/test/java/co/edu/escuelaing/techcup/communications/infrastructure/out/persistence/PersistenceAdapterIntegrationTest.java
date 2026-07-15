package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.MessageStatus;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ChatRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.MessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ReportedMessageRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trips the two aggregates whose persistence mapper needs a fully hydrated cross-aggregate
 * reference rather than an id-only stub (see {@link SupportTicketPersistenceMapper} and
 * {@link ReportedMessagePersistenceMapper}). A lightweight stub here would silently corrupt data
 * on save: dropping every existing chat participant (orphanRemoval) or nulling out a message's
 * required columns. Runs against a real Postgres (docker compose up postgres), matching the rest
 * of this project's test convention - no mocks, since these two paths are exactly what
 * mock-based service tests cannot catch.
 */
@SpringBootTest
@Transactional
class PersistenceAdapterIntegrationTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReportedMessageRepository reportedMessageRepository;

    @Test
    void replyingToAReloadedSupportTicketKeepsExistingParticipants() {
        UUID requesterId = UUID.randomUUID();
        UUID botId = UUID.randomUUID();
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(requesterId, ParticipantRole.MEMBER);
        chat.addParticipant(botId, ParticipantRole.MEMBER);
        chat = chatRepository.save(chat);

        SupportTicket ticket = supportTicketRepository.save(SupportTicket.open(chat, requesterId, "Help me"));

        // Mirrors ReplySupportTicketService: reload the ticket, traverse to its chat, mutate, save.
        SupportTicket reloaded = supportTicketRepository.findById(ticket.getId()).orElseThrow();
        Chat reloadedChat = reloaded.getChat();
        assertThat(reloadedChat.isParticipant(requesterId)).isTrue();
        assertThat(reloadedChat.isParticipant(botId)).isTrue();

        UUID agentId = UUID.randomUUID();
        reloadedChat.addParticipant(agentId, ParticipantRole.MEMBER);
        chatRepository.save(reloadedChat);
        messageRepository.save(reloadedChat.postMessage(agentId, "how can I help?"));

        Chat finalChat = chatRepository.findById(chat.getId()).orElseThrow();
        assertThat(finalChat.getParticipants()).hasSize(3);
        assertThat(finalChat.isParticipant(requesterId)).isTrue();
        assertThat(finalChat.isParticipant(botId)).isTrue();
        assertThat(finalChat.isParticipant(agentId)).isTrue();
    }

    @Test
    void resolvingAReportWithDeleteMessagePreservesTheMessageFields() {
        UUID senderId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(senderId, ParticipantRole.MEMBER);
        chat.addParticipant(reporterId, ParticipantRole.MEMBER);
        chat = chatRepository.save(chat);

        Message message = messageRepository.save(chat.postMessage(senderId, "bad content"));
        ReportedMessage report = reportedMessageRepository.save(
                ReportedMessage.create(message, reporterId, "spam"));

        // Mirrors ResolveReportService: reload the report, mark its message deleted, save it back.
        ReportedMessage reloadedReport = reportedMessageRepository.findById(report.getId()).orElseThrow();
        reloadedReport.getMessage().markDeleted();
        messageRepository.save(reloadedReport.getMessage());

        Message reloadedMessage = messageRepository.findById(message.getId()).orElseThrow();
        assertThat(reloadedMessage.getStatus()).isEqualTo(MessageStatus.DELETED);
        assertThat(reloadedMessage.getContent()).isEqualTo("bad content");
        assertThat(reloadedMessage.getSenderId()).isEqualTo(senderId);
        assertThat(reloadedMessage.getChatId()).isEqualTo(chat.getId());
    }
}
