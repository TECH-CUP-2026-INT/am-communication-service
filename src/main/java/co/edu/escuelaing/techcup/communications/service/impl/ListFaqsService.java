package co.edu.escuelaing.techcup.communications.service.impl;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import co.edu.escuelaing.techcup.communications.repository.FaqRepository;
import co.edu.escuelaing.techcup.communications.service.ListFaqsUseCase;
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
