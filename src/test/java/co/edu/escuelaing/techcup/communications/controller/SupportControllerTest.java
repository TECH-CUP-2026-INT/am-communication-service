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
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void createReturns201() throws Exception {
        SupportTicket ticket = sampleTicket();
        when(createSupportTicketUseCase.create(any())).thenReturn(ticket);
        CreateSupportTicketRequest request = new CreateSupportTicketRequest(requester, "cannot login");

        mockMvc.perform(post("/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentLevel").value("CHATBOT"))
                .andExpect(jsonPath("$.requesterId").value(requester.toString()));
    }

    @Test
    void replyReturns201() throws Exception {
        SupportTicket ticket = sampleTicket();
        Message message = ticket.getChat().postMessage(requester, "any update?");
        when(replySupportTicketUseCase.reply(any())).thenReturn(message);
        ReplySupportTicketRequest request = new ReplySupportTicketRequest(requester, "any update?");

        mockMvc.perform(post("/support/tickets/{id}/reply", ticket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("any update?"));
    }

    @Test
    void escalateReturns200WithNewLevel() throws Exception {
        SupportTicket ticket = sampleTicket();
        ticket.escalateTo(SupportLevel.AUTOMATIC);
        when(escalateConversationUseCase.escalate(eq(ticket.getId()))).thenReturn(ticket);

        mockMvc.perform(post("/support/tickets/{id}/escalate", ticket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value("AUTOMATIC"))
                .andExpect(jsonPath("$.status").value("ESCALATED"));
    }

    @Test
    void escalateMissingTicketReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(escalateConversationUseCase.escalate(eq(id)))
                .thenThrow(new SupportTicketNotFoundException(id));

        mockMvc.perform(post("/support/tickets/{id}/escalate", id))
                .andExpect(status().isNotFound());
    }
}
