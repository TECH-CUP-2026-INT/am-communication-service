package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.CreateSupportTicketRequest;
import co.edu.escuelaing.techcup.communications.dto.ReplySupportTicketRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.SupportTicket;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.entity.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapperImpl;
import co.edu.escuelaing.techcup.communications.mapper.SupportMapperImpl;
import co.edu.escuelaing.techcup.communications.service.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.service.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.service.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.service.command.ReplySupportTicketCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static co.edu.escuelaing.techcup.communications.controller.TestCallers.caller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SupportMapperImpl.class, MessageMapperImpl.class})
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateSupportTicketUseCase createSupportTicketUseCase;

    @MockitoBean
    private ReplySupportTicketUseCase replySupportTicketUseCase;

    @MockitoBean
    private EscalateConversationUseCase escalateConversationUseCase;

    private final UUID requester = UUID.randomUUID();

    private SupportTicket sampleTicket() {
        Chat chat = Chat.create(ChatType.SUPPORT, null);
        chat.addParticipant(requester, ParticipantRole.MEMBER);
        return SupportTicket.open(chat, requester, "cannot login");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReturns201AndTakesTheRequesterFromTheToken() throws Exception {
        SupportTicket ticket = sampleTicket();
        when(createSupportTicketUseCase.create(any())).thenReturn(ticket);
        CreateSupportTicketRequest request = new CreateSupportTicketRequest("cannot login");

        mockMvc.perform(post("/support/tickets")
                        .with(caller(requester, ParticipantRole.MEMBER.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentLevel").value("CHATBOT"))
                .andExpect(jsonPath("$.requesterId").value(requester.toString()));

        ArgumentCaptor<CreateSupportTicketCommand> captor = ArgumentCaptor.forClass(CreateSupportTicketCommand.class);
        verify(createSupportTicketUseCase).create(captor.capture());
        assertThat(captor.getValue().requesterId()).isEqualTo(requester);
    }

    @Test
    void replyReturns201AndTakesTheSenderFromTheToken() throws Exception {
        SupportTicket ticket = sampleTicket();
        Message message = ticket.getChat().postMessage(requester, "any update?");
        when(replySupportTicketUseCase.reply(any())).thenReturn(message);
        ReplySupportTicketRequest request = new ReplySupportTicketRequest("any update?");

        mockMvc.perform(post("/support/tickets/{id}/reply", ticket.getId())
                        .with(caller(requester))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("any update?"));

        ArgumentCaptor<ReplySupportTicketCommand> captor = ArgumentCaptor.forClass(ReplySupportTicketCommand.class);
        verify(replySupportTicketUseCase).reply(captor.capture());
        assertThat(captor.getValue().senderId()).isEqualTo(requester);
        assertThat(captor.getValue().ticketId()).isEqualTo(ticket.getId());
    }

    @Test
    void escalateReturns200WithNewLevel() throws Exception {
        SupportTicket ticket = sampleTicket();
        ticket.escalateTo(SupportLevel.AUTOMATIC);
        when(escalateConversationUseCase.escalate(eq(ticket.getId()))).thenReturn(ticket);

        mockMvc.perform(post("/support/tickets/{id}/escalate", ticket.getId())
                        .with(caller(requester, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value("AUTOMATIC"))
                .andExpect(jsonPath("$.status").value("ESCALATED"));
    }

    @Test
    void escalateMissingTicketReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(escalateConversationUseCase.escalate(eq(id)))
                .thenThrow(new SupportTicketNotFoundException(id));

        mockMvc.perform(post("/support/tickets/{id}/escalate", id)
                        .with(caller(requester, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isNotFound());
    }
}
