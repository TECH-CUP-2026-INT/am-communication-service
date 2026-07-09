package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.ReportMessageRequest;
import co.edu.escuelaing.techcup.communications.dto.SendMessageRequest;
import co.edu.escuelaing.techcup.communications.entity.Chat;
import co.edu.escuelaing.techcup.communications.entity.Message;
import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ChatType;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.ChatClosedException;
import co.edu.escuelaing.techcup.communications.exception.MessageAlreadyReportedException;
import co.edu.escuelaing.techcup.communications.exception.MessageNotFoundException;
import co.edu.escuelaing.techcup.communications.exception.ParticipantNotAllowedException;
import co.edu.escuelaing.techcup.communications.mapper.MessageMapperImpl;
import co.edu.escuelaing.techcup.communications.mapper.ReportMapperImpl;
import co.edu.escuelaing.techcup.communications.service.ReportMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.SendMessageUseCase;
import co.edu.escuelaing.techcup.communications.service.command.ReportMessageCommand;
import co.edu.escuelaing.techcup.communications.service.command.SendMessageCommand;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({MessageMapperImpl.class, ReportMapperImpl.class})
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendMessageUseCase sendMessageUseCase;

    @MockitoBean
    private ReportMessageUseCase reportMessageUseCase;

    private final UUID sender = UUID.randomUUID();
    private final UUID reporter = UUID.randomUUID();

    private Message sampleMessage() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        return chat.postMessage(sender, "hello");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendReturns201AndTakesTheSenderFromTheToken() throws Exception {
        Message message = sampleMessage();
        when(sendMessageUseCase.send(any())).thenReturn(message);
        SendMessageRequest request = new SendMessageRequest(message.getChatId(), "hello");

        mockMvc.perform(post("/messages")
                        .with(caller(sender, ParticipantRole.MEMBER.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.senderId").value(sender.toString()));

        ArgumentCaptor<SendMessageCommand> captor = ArgumentCaptor.forClass(SendMessageCommand.class);
        verify(sendMessageUseCase).send(captor.capture());
        assertThat(captor.getValue().senderId()).isEqualTo(sender);
    }

    @Test
    void rejectsBlankContentWith400() throws Exception {
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), "  ");

        mockMvc.perform(post("/messages")
                        .with(caller(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonParticipantReturns403() throws Exception {
        when(sendMessageUseCase.send(any()))
                .thenThrow(new ParticipantNotAllowedException(sender, UUID.randomUUID()));
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), "hi");

        mockMvc.perform(post("/messages")
                        .with(caller(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void closedChatReturns409() throws Exception {
        when(sendMessageUseCase.send(any())).thenThrow(new ChatClosedException(UUID.randomUUID()));
        SendMessageRequest request = new SendMessageRequest(UUID.randomUUID(), "hi");

        mockMvc.perform(post("/messages")
                        .with(caller(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void reportReturns201AndTakesTheReporterFromTheToken() throws Exception {
        Message message = sampleMessage();
        ReportedMessage report = ReportedMessage.create(message, reporter, "spam");
        when(reportMessageUseCase.report(any())).thenReturn(report);
        ReportMessageRequest request = new ReportMessageRequest("spam");

        mockMvc.perform(post("/messages/{id}/report", message.getId())
                        .with(caller(reporter))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporterId").value(reporter.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<ReportMessageCommand> captor = ArgumentCaptor.forClass(ReportMessageCommand.class);
        verify(reportMessageUseCase).report(captor.capture());
        assertThat(captor.getValue().reporterId()).isEqualTo(reporter);
    }

    @Test
    void reportDuplicateReturns409() throws Exception {
        when(reportMessageUseCase.report(any())).thenThrow(new MessageAlreadyReportedException(UUID.randomUUID()));
        ReportMessageRequest request = new ReportMessageRequest("spam");

        mockMvc.perform(post("/messages/{id}/report", UUID.randomUUID())
                        .with(caller(reporter))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void reportMissingMessageReturns404() throws Exception {
        when(reportMessageUseCase.report(any())).thenThrow(new MessageNotFoundException(UUID.randomUUID()));
        ReportMessageRequest request = new ReportMessageRequest("spam");

        mockMvc.perform(post("/messages/{id}/report", UUID.randomUUID())
                        .with(caller(reporter))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
