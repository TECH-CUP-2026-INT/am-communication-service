package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.CreateFaqUseCase;
import co.edu.escuelaing.techcup.communications.application.usecase.command.CreateFaqCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFaqService implements CreateFaqUseCase {

    private final FaqRepository faqRepository;

    @Override
    @Transactional
    public Faq create(CreateFaqCommand command) {
        return faqRepository.save(Faq.create(command.keywords(), command.answer()));
    }
}
