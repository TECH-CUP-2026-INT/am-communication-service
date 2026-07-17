package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.application.usecase.command.UpdateFaqCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private UpdateFaqService service;

    @Test
    void updatesExistingFaq() {
        Faq faq = Faq.create(Set.of("password"), "old answer");
        when(faqRepository.findById(faq.getId())).thenReturn(Optional.of(faq));
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        Faq result = service.update(new UpdateFaqCommand(faq.getId(), Set.of("billing"), "new answer"));

        assertThat(result.getKeywords()).containsExactly("billing");
        assertThat(result.getAnswer()).isEqualTo("new answer");
    }

    @Test
    void throwsWhenFaqNotFound() {
        UUID id = UUID.randomUUID();
        when(faqRepository.findById(id)).thenReturn(Optional.empty());

        UpdateFaqCommand command = new UpdateFaqCommand(id, Set.of("password"), "answer");
        assertThatThrownBy(() -> service.update(command))
                .isInstanceOf(FaqNotFoundException.class);
    }
}
