package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.UpdateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.command.UpdateFaqCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFaqService implements UpdateFaqUseCase {

    private final FaqRepository faqRepository;

    @Override
    @Transactional
    public Faq update(UpdateFaqCommand command) {
        Faq faq = faqRepository.findById(command.faqId())
                .orElseThrow(() -> new FaqNotFoundException(command.faqId()));
        faq.update(command.keywords(), command.answer());
        return faqRepository.save(faq);
    }
}
