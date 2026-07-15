package co.edu.escuelaing.techcup.communications.domain.service.ports.in;

import java.util.UUID;

public interface DeleteFaqUseCase {

    void delete(UUID faqId);
}
