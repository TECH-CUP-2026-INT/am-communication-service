package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.GetFaqUseCase;
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
