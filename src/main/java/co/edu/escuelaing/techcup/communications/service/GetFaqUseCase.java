package co.edu.escuelaing.techcup.communications.service;

import co.edu.escuelaing.techcup.communications.entity.Faq;

import java.util.UUID;

public interface GetFaqUseCase {

    Faq getById(UUID faqId);
}
