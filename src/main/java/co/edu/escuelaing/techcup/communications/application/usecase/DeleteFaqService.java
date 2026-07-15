package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.DeleteFaqUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteFaqService implements DeleteFaqUseCase {

    private final FaqRepository faqRepository;

    @Override
    @Transactional
    public void delete(UUID faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new FaqNotFoundException(faqId));
        faqRepository.delete(faq);
    }
}
