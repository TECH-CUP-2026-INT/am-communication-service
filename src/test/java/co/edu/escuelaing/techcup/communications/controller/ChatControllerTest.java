package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.dto.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapperImpl;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapperImpl;
import co.edu.escuelaing.techcup.communications.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.service.CloseChatUseCase;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatMessagesUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static co.edu.escuelaing.techcup.communications.controller.TestCallers.caller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ChatMapperImpl.class, MessageMapperImpl.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateChatUseCase createChatUseCase;

    @MockitoBean
    private GetChatUseCase getChatUseCase;

    @MockitoBean
    private GetChatMessagesUseCase getChatMessagesUseCase;

    @MockitoBean
    private CloseChatUseCase closeChatUseCase;

    private final UUID userA = UUID.randomUUID();

    private Chat sampleChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);
        return chat;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsChatReturns201WithLocation() throws Exception {
        Chat chat = sampleChat();
        when(createChatUseCase.create(any())).thenReturn(chat);
        CreateChatRequest request = new CreateChatRequest(ChatType.DIRECT, null,
                List.of(new ParticipantRequest(userA, ParticipantRole.MEMBER)));

        mockMvc.perform(post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/chats/" + chat.getId())))
                .andExpect(jsonPath("$.id").value(chat.getId().toString()))
                .andExpect(jsonPath("$.type").value("DIRECT"))
                .andExpect(jsonPath("$.participants[0].userId").value(userA.toString()));
    }

    @Test
    void rejectsInvalidRequestWith400() throws Exception {
        CreateChatRequest invalid = new CreateChatRequest(null, null, List.of());

        mockMvc.perform(post("/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getByIdReturnsChat() throws Exception {
        Chat chat = sampleChat();
        when(getChatUseCase.getById(chat.getId(), userA)).thenReturn(chat);

        mockMvc.perform(get("/chats/{id}", chat.getId()).with(caller(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chat.getId().toString()));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(getChatUseCase.getById(id, userA)).thenThrow(new ChatNotFoundException(id));

        mockMvc.perform(get("/chats/{id}", id).with(caller(userA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMessagesReturnsPagedResponse() throws Exception {
        Chat chat = sampleChat();
        Message message = chat.postMessage(userA, "hello");
        when(getChatMessagesUseCase.getByChat(eq(chat.getId()), any(), eq(userA)))
                .thenReturn(new PageImpl<>(List.of(message), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/chats/{id}/messages", chat.getId()).with(caller(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].senderId").value(userA.toString()));
    }

    @Test
    void closeReturnsClosedChat() throws Exception {
        Chat chat = sampleChat();
        chat.close();
        when(closeChatUseCase.close(chat.getId(), userA)).thenReturn(chat);

        mockMvc.perform(post("/chats/{id}/close", chat.getId()).with(caller(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void closeAlreadyClosedReturns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(closeChatUseCase.close(id, userA)).thenThrow(new InvalidChatOperationException("already closed"));

        mockMvc.perform(post("/chats/{id}/close", id).with(caller(userA)))
                .andExpect(status().isConflict());
    }
}
