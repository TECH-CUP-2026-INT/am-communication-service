package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private DeleteFaqService service;

    @Test
    void deletesExistingFaq() {
        Faq faq = Faq.create(Set.of("password"), "answer");
        when(faqRepository.findById(faq.getId())).thenReturn(Optional.of(faq));

        service.delete(faq.getId());

        verify(faqRepository).delete(faq);
    }

    @Test
    void throwsWhenFaqNotFound() {
        UUID id = UUID.randomUUID();
        when(faqRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(FaqNotFoundException.class);
    }
}
