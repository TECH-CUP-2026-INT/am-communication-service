package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByChat_Id(UUID chatId, Pageable pageable);

    List<Message> findByChat_IdOrderBySentAtAsc(UUID chatId);
}
