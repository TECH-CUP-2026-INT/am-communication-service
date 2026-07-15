package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ReportedMessageDao;
import org.mapstruct.Mapper;

/**
 * Maps {@link ReportedMessageDao} to/from {@link ReportedMessage}. Unlike {@link Message#chat},
 * the {@code message} association here must be the fully hydrated {@link Message} - not a
 * reference stub - because {@code ResolveReportService} calls {@code getMessage().markDeleted()}
 * and saves it back; a stub would round-trip nulls into NOT NULL columns. Hydration is composed
 * by {@code ReportedMessageRepositoryAdapter} (which already depends on the message repository)
 * and passed in, keeping this mapper stateless.
 */
@Mapper(componentModel = "spring")
public interface ReportedMessagePersistenceMapper {

    default ReportedMessage toDomain(ReportedMessageDao dao, Message hydratedMessage) {
        return ReportedMessage.fromPersistence(dao.getId(), hydratedMessage, dao.getReporterId(), dao.getReason(),
                dao.getStatus(), dao.getResolution(), dao.getCreatedAt(), dao.getReviewedAt());
    }

    default ReportedMessageDao toDao(ReportedMessage report) {
        ReportedMessageDao dao = new ReportedMessageDao();
        dao.setId(report.getId());
        dao.setReporterId(report.getReporterId());
        dao.setReason(report.getReason());
        dao.setStatus(report.getStatus());
        dao.setResolution(report.getResolution());
        dao.setCreatedAt(report.getCreatedAt());
        dao.setReviewedAt(report.getReviewedAt());
        return dao;
    }
}
