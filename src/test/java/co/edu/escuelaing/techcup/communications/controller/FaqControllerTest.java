package co.edu.escuelaing.techcup.communications.controller;

import co.edu.escuelaing.techcup.communications.dto.FaqRequest;
import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.entity.enums.ParticipantRole;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.mapper.FaqMapperImpl;
import co.edu.escuelaing.techcup.communications.service.CreateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.DeleteFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.GetFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.ListFaqsUseCase;
import co.edu.escuelaing.techcup.communications.service.UpdateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.command.UpdateFaqCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static co.edu.escuelaing.techcup.communications.controller.TestCallers.caller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FaqController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FaqMapperImpl.class)
class FaqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateFaqUseCase createFaqUseCase;

    @MockitoBean
    private UpdateFaqUseCase updateFaqUseCase;

    @MockitoBean
    private DeleteFaqUseCase deleteFaqUseCase;

    @MockitoBean
    private GetFaqUseCase getFaqUseCase;

    @MockitoBean
    private ListFaqsUseCase listFaqsUseCase;

    private final UUID moderator = UUID.randomUUID();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        Faq faq = Faq.create(Set.of("password", "login"), "Reset it from the login screen.");
        when(createFaqUseCase.create(any())).thenReturn(faq);
        FaqRequest request = new FaqRequest(Set.of("password", "login"), "Reset it from the login screen.");

        mockMvc.perform(post("/faqs")
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/faqs/" + faq.getId())))
                .andExpect(jsonPath("$.answer").value("Reset it from the login screen."));
    }

    @Test
    void createRejectsBlankAnswer() throws Exception {
        FaqRequest request = new FaqRequest(Set.of("password"), " ");

        mockMvc.perform(post("/faqs")
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsAllFaqs() throws Exception {
        Faq faq = Faq.create(Set.of("password"), "answer");
        when(listFaqsUseCase.listAll()).thenReturn(List.of(faq));

        mockMvc.perform(get("/faqs")
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(faq.getId().toString()));
    }

    @Test
    void getByIdReturnsFaq() throws Exception {
        Faq faq = Faq.create(Set.of("password"), "answer");
        when(getFaqUseCase.getById(faq.getId())).thenReturn(faq);

        mockMvc.perform(get("/faqs/{id}", faq.getId())
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("answer"));
    }

    @Test
    void getByIdMissingReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(getFaqUseCase.getById(id)).thenThrow(new FaqNotFoundException(id));

        mockMvc.perform(get("/faqs/{id}", id)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturns200AndUsesPathId() throws Exception {
        UUID id = UUID.randomUUID();
        Faq updated = Faq.create(Set.of("billing"), "new answer");
        when(updateFaqUseCase.update(any())).thenReturn(updated);
        FaqRequest request = new FaqRequest(Set.of("billing"), "new answer");

        mockMvc.perform(put("/faqs/{id}", id)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("new answer"));

        ArgumentCaptor<UpdateFaqCommand> captor = ArgumentCaptor.forClass(UpdateFaqCommand.class);
        verify(updateFaqUseCase).update(captor.capture());
        assertThat(captor.getValue().faqId()).isEqualTo(id);
    }

    @Test
    void updateMissingReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(updateFaqUseCase.update(any())).thenThrow(new FaqNotFoundException(id));
        FaqRequest request = new FaqRequest(Set.of("billing"), "new answer");

        mockMvc.perform(put("/faqs/{id}", id)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/faqs/{id}", id)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isNoContent());

        verify(deleteFaqUseCase).delete(id);
    }

    @Test
    void deleteMissingReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new FaqNotFoundException(id)).when(deleteFaqUseCase).delete(id);

        mockMvc.perform(delete("/faqs/{id}", id)
                        .with(caller(moderator, ParticipantRole.MODERATOR.name())))
                .andExpect(status().isNotFound());
    }
}
