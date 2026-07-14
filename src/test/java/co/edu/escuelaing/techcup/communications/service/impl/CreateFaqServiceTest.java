package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.command.CreateFaqCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private CreateFaqService service;

    @Test
    void createsAndPersistsFaq() {
        when(faqRepository.save(any(Faq.class))).thenAnswer(inv -> inv.getArgument(0));

        Faq faq = service.create(new CreateFaqCommand(Set.of("password", "login"), "Reset it here."));

        assertThat(faq.getKeywords()).containsExactlyInAnyOrder("password", "login");
        assertThat(faq.getAnswer()).isEqualTo("Reset it here.");
    }
}
