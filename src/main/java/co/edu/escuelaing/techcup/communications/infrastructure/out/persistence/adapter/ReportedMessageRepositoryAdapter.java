package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.MessagePersistenceMapper;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.ReportedMessagePersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.Message;
import co.edu.escuelaing.techcup.communications.domain.model.ReportedMessage;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ReportedMessageDao;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.MessageJpaRepository;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ReportedMessageJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ReportedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ReportedMessageRepositoryAdapter implements ReportedMessageRepository {

    private final ReportedMessageJpaRepository jpaRepository;
    private final MessageJpaRepository messageJpaRepository;
    private final ReportedMessagePersistenceMapper mapper;
    private final MessagePersistenceMapper messageMapper;

    @Override
    public Optional<ReportedMessage> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public ReportedMessage save(ReportedMessage report) {
        ReportedMessageDao dao = mapper.toDao(report);
        dao.setMessage(messageJpaRepository.getReferenceById(report.getMessageId()));
        return toDomain(jpaRepository.save(dao));
    }

    @Override
    public boolean existsByMessage_IdAndReporterId(UUID messageId, UUID reporterId) {
        return jpaRepository.existsByMessage_IdAndReporterId(messageId, reporterId);
    }

    /** {@code message} must be fully hydrated - see {@link ReportedMessagePersistenceMapper}. */
    private ReportedMessage toDomain(ReportedMessageDao dao) {
        Message message = messageMapper.toDomain(dao.getMessage());
        return mapper.toDomain(dao, message);
    }
}
