package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.exception.FaqNotFoundException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.UpdateFaqUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.UpdateFaqCommand;
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
