package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.CreateSupportTicketRequest;
import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ReplySupportTicketRequest;
import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.SupportTicket;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.model.enums.SupportLevel;
import co.edu.escuelaing.techcup.communications.domain.exception.SupportTicketNotFoundException;
import co.edu.escuelaing.techcup.communications.application.mapper.MessageMapperImpl;
import co.edu.escuelaing.techcup.communications.application.mapper.SupportMapperImpl;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateSupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.EscalateConversationUseCase;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ReplySupportTicketUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateSupportTicketCommand;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ReplySupportTicketCommand;
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

import static co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller.TestCallers.caller;
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
                .andExpect(jsonPath("$.currentLevel").value("FAQ"))
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
        ticket.escalateTo(SupportLevel.CHATBOT);
        when(escalateConversationUseCase.escalate(eq(ticket.getId()), eq(requester))).thenReturn(ticket);

        mockMvc.perform(post("/support/tickets/{id}/escalate", ticket.getId())
                        .with(caller(requester, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value("CHATBOT"))
                .andExpect(jsonPath("$.status").value("ESCALATED"));
    }

    @Test
    void escalateMissingTicketReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(escalateConversationUseCase.escalate(eq(id), eq(requester)))
                .thenThrow(new SupportTicketNotFoundException(id));

        mockMvc.perform(post("/support/tickets/{id}/escalate", id)
                        .with(caller(requester, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isNotFound());
    }
}
