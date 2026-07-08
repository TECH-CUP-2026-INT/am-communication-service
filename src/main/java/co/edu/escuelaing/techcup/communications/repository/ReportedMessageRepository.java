package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.ReportedMessage;
import co.edu.escuelaing.techcup.communications.entity.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportedMessageRepository extends JpaRepository<ReportedMessage, UUID> {

    boolean existsByMessage_IdAndReporterId(UUID messageId, UUID reporterId);

    List<ReportedMessage> findByMessage_Id(UUID messageId);

    List<ReportedMessage> findByStatus(ReportStatus status);
}
