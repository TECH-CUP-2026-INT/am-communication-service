package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.CreateFaqUseCase;
import co.edu.escuelaing.techcup.communications.service.command.CreateFaqCommand;
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
