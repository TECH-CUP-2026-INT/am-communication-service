package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.MessageDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageJpaRepository extends JpaRepository<MessageDao, UUID> {

    Page<MessageDao> findByChat_Id(UUID chatId, Pageable pageable);

    List<MessageDao> findByChat_IdOrderBySentAtAsc(UUID chatId);
}
