package co.edu.escuelaing.techcup.communications.mapper;

import co.edu.escuelaing.techcup.communications.dto.ChatResponse;
import co.edu.escuelaing.techcup.communications.dto.ParticipantResponse;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.repository.ChatRepository;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import co.edu.escuelaing.techcup.communications.service.GetUserChatsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Controllers map entities once the service transaction has ended and open-in-view is
 * disabled, so the participants of a chat must already be loaded by then.
 */
@SpringBootTest
class ChatMappingOutsideTransactionTest {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private GetChatUseCase getChatUseCase;

    @Autowired
    private GetUserChatsUseCase getUserChatsUseCase;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final UUID member = UUID.randomUUID();
    private final UUID moderator = UUID.randomUUID();

    private UUID chatId;

    @BeforeEach
    void persistChat() {
        chatId = transactionTemplate.execute(status -> {
            Chat chat = Chat.create(ChatType.DIRECT, null);
            chat.addParticipant(member, ParticipantRole.MEMBER);
            chat.addParticipant(moderator, ParticipantRole.MODERATOR);
            return chatRepository.save(chat).getId();
        });
    }

    @Test
    void mapsASingleChatWithItsParticipants() {
        ChatResponse response = chatMapper.toResponse(getChatUseCase.getById(chatId));

        assertThat(response.id()).isEqualTo(chatId);
        assertThat(response.participants()).extracting(ParticipantResponse::userId)
                .containsExactlyInAnyOrder(member, moderator);
    }

    @Test
    void mapsEveryParticipantOfAUserChatAndNotOnlyTheMatchingOne() {
        List<ChatResponse> responses = getUserChatsUseCase.getByUser(member).stream()
                .map(chatMapper::toResponse)
                .toList();

        assertThat(responses).singleElement()
                .satisfies(response -> assertThat(response.participants())
                        .extracting(ParticipantResponse::userId)
                        .containsExactlyInAnyOrder(member, moderator));
    }
}
