package co.edu.escuelaing.techcup.communications.infrastructure.in.rest.controller;

import co.edu.escuelaing.techcup.communications.infrastructure.in.rest.dto.request.ResolveReportRequest;
import co.edu.escuelaing.techcup.communications.domain.model.Chat;
import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ChatType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ModeratorActionType;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.domain.exception.InvalidChatOperationException;
import co.edu.escuelaing.techcup.communications.domain.exception.ReportedMessageNotFoundException;
import co.edu.escuelaing.techcup.communications.application.mapper.ReportMapperImpl;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ResolveReportUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.ResolveReportCommand;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ReportMapperImpl.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResolveReportUseCase resolveReportUseCase;

    private final UUID moderator = UUID.randomUUID();

    private ReportedMessage resolvedReport() {
        Chat chat = Chat.create(ChatType.DIRECT, null);
        UUID sender = UUID.randomUUID();
        chat.addParticipant(sender, ParticipantRole.MEMBER);
        Message message = chat.postMessage(sender, "offensive");
        message.markReported();
        ReportedMessage report = ReportedMessage.create(message, UUID.randomUUID(), "spam");
        report.resolve(ReportStatus.ACTIONED, "removed");
        return report;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveReturns200AndTakesTheModeratorFromTheToken() throws Exception {
        when(resolveReportUseCase.resolve(any())).thenReturn(resolvedReport());
        ResolveReportRequest request = new ResolveReportRequest(
                ReportStatus.ACTIONED, "removed", ModeratorActionType.DELETE_MESSAGE);

        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIONED"));

        ArgumentCaptor<ResolveReportCommand> captor = ArgumentCaptor.forClass(ResolveReportCommand.class);
        verify(resolveReportUseCase).resolve(captor.capture());
        assertThat(captor.getValue().moderatorId()).isEqualTo(moderator);
    }

    @Test
    void resolveMissingReportReturns404() throws Exception {
        when(resolveReportUseCase.resolve(any())).thenThrow(new ReportedMessageNotFoundException(UUID.randomUUID()));
        ResolveReportRequest request = new ResolveReportRequest(
                ReportStatus.DISMISSED, "x", ModeratorActionType.WARN);

        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolveAlreadyResolvedReturns409() throws Exception {
        when(resolveReportUseCase.resolve(any())).thenThrow(new InvalidChatOperationException("already resolved"));
        ResolveReportRequest request = new ResolveReportRequest(
                ReportStatus.DISMISSED, "x", ModeratorActionType.WARN);

        mockMvc.perform(post("/reports/{id}/resolve", UUID.randomUUID())
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
