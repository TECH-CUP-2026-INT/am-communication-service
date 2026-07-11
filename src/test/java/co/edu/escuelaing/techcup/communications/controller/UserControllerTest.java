package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.mapper.ChatMapperImpl;
import co.edu.escuelaing.techcup.communications.service.GetUserChatsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ChatMapperImpl.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetUserChatsUseCase getUserChatsUseCase;

    @Test
    void returnsUserChats() throws Exception {
        UUID userId = UUID.randomUUID();
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userId, ParticipantRole.MEMBER);
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of(chat));

        mockMvc.perform(get("/users/{id}/chats", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chat.getId().toString()))
                .andExpect(jsonPath("$[0].participants[0].userId").value(userId.toString()));
    }

    @Test
    void returnsEmptyListWhenNoChats() throws Exception {
        UUID userId = UUID.randomUUID();
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/users/{id}/chats", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
