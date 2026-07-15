package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for Faq persistence. */
public interface FaqRepository {

    List<Faq> findAllByOrderByCreatedAtAsc();

    Faq save(Faq faq);

    Optional<Faq> findById(UUID id);

    void delete(Faq faq);
}
