package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFaqsServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private ListFaqsService service;

    @Test
    void returnsAllFaqsOrderedByCreatedAt() {
        Faq first = Faq.create(Set.of("password"), "first");
        Faq second = Faq.create(Set.of("billing"), "second");
        when(faqRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(first, second));

        assertThat(service.listAll()).containsExactly(first, second);
    }
}
