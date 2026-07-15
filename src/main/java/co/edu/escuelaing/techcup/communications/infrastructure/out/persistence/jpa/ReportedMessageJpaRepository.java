package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.domain.model.enums.ReportStatus;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ReportedMessageDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportedMessageJpaRepository extends JpaRepository<ReportedMessageDao, UUID> {

    boolean existsByMessage_IdAndReporterId(UUID messageId, UUID reporterId);

    List<ReportedMessageDao> findByMessage_Id(UUID messageId);

    List<ReportedMessageDao> findByStatus(ReportStatus status);
}
