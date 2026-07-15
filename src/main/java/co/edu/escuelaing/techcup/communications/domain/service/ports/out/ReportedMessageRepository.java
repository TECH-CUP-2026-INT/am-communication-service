package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;

import java.util.Optional;
import java.util.UUID;

/** Outbound port for ReportedMessage persistence. */
public interface ReportedMessageRepository {

    Optional<ReportedMessage> findById(UUID id);

    ReportedMessage save(ReportedMessage report);

    boolean existsByMessage_IdAndReporterId(UUID messageId, UUID reporterId);
}
