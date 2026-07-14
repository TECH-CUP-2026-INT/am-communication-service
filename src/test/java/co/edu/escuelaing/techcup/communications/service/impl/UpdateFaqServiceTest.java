package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.command.UpdateFaqCommand;
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

        assertThatThrownBy(() -> service.update(new UpdateFaqCommand(id, Set.of("password"), "answer")))
                .isInstanceOf(FaqNotFoundException.class);
    }
}
