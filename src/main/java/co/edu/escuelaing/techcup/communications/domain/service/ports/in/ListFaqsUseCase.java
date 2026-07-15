package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;

import java.util.List;

public interface ListFaqsUseCase {

    List<Faq> listAll();
}
