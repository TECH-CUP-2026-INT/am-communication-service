package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.GetFaqUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFaqService implements GetFaqUseCase {

    private final FaqRepository faqRepository;

    @Override
    @Transactional(readOnly = true)
    public Faq getById(UUID faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new FaqNotFoundException(faqId));
    }
}
