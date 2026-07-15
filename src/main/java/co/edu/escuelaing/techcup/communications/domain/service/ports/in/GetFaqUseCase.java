package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;

import java.util.UUID;

public interface GetFaqUseCase {

    Faq getById(UUID faqId);
}
