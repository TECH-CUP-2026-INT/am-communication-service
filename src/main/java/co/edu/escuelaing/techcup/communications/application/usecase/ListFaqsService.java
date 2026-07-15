package co.edu.escuelaing.techcup.communications.application.usecase;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.in.ListFaqsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListFaqsService implements ListFaqsUseCase {

    private final FaqRepository faqRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Faq> listAll() {
        return faqRepository.findAllByOrderByCreatedAtAsc();
    }
}
