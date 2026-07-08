package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapperImpl;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessageMapperImpl.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendMessageUseCase sendMessageUseCase;

    private final UUID sender = UUID.randomUUID();

    private Message sampleMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat.postMessage(sender, "hello");
    }

    @Test
    void sendReturns201() throws Exception {
        Message message = sampleMessage();
        when(sendMessageUseCase.send(any())).thenReturn(message);
        SendMessageRequest request = new SendMessageRequest(message.getChatId(), sender, "hello");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.senderId").value(sender.toString()));
    }

    @Test
    void rejectsBlankContentWith400() throws Exception {
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), sender, "  ");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonParticipantReturns403() throws Exception {
        when(sendMessageUseCase.send(any()))
                .thenThrow(new ParticipantNotAllowedException(sender, UUID.randomUUID()));
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), sender, "hi");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void closedChatReturns409() throws Exception {
        when(sendMessageUseCase.send(any())).thenThrow(new ChatClosedException(UUID.randomUUID()));
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), sender, "hi");

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
