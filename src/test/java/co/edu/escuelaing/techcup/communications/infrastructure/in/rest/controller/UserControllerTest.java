package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.application.mapper.ChatMapperImpl;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetUserChatsUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.TestCallers.caller;
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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsOwnChatsForTheCaller() throws Exception {
        UUID userId = UUID.randomUUID();
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(userId, ParticipantRole.MEMBER);
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of(chat));

        mockMvc.perform(get("/users/{id}/chats", userId).with(caller(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(chat.getId().toString()))
                .andExpect(jsonPath("$[0].participants[0].userId").value(userId.toString()));
    }

    @Test
    void returnsEmptyListWhenNoChats() throws Exception {
        UUID userId = UUID.randomUUID();
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/users/{id}/chats", userId).with(caller(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deniesLookingUpAnotherUsersChatsWithoutAPrivilegedRole() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID stranger = UUID.randomUUID();

        mockMvc.perform(get("/users/{id}/chats", userId).with(caller(stranger, ParticipantRole.MEMBER.name())))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAModeratorToLookUpAnotherUsersChats() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderator = UUID.randomUUID();
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/users/{id}/chats", userId)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAnIdentityAdminToLookUpAnotherUsersChats() throws Exception {
        // cc-identity-service has no MODERATOR role; ADMIN is its equivalent administrative role.
        UUID userId = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        when(getUserChatsUseCase.getByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/users/{id}/chats", userId).with(caller(admin, "ADMIN")))
                .andExpect(status().isOk());
    }
}
