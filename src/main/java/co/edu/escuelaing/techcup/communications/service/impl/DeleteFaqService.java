package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.DeleteFaqUseCase;
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
