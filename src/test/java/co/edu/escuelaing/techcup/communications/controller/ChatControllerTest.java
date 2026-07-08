package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.CreateChatRequest;
import co.edu.escuelaing.techcup.communications.dto.ParticipantRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.ChatNotFoundException;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapperImpl;
import co.edu.escuelaing.techcup.communications.service.CreateChatUseCase;
import co.edu.escuelaing.techcup.communications.service.GetChatUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ChatMapperImpl.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateChatUseCase createChatUseCase;

    @MockitoBean
    private GetChatUseCase getChatUseCase;

    private final UUID userA = UUID.randomUUID();

    private Chat sampleChat() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userA, ParticipantRole.MEMBER);
        return chat;
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
        when(getChatUseCase.getById(chat.getId())).thenReturn(chat);

        mockMvc.perform(get("/chats/{id}", chat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chat.getId().toString()));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(getChatUseCase.getById(id)).thenThrow(new ChatNotFoundException(id));

        mockMvc.perform(get("/chats/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
