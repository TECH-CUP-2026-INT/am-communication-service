package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ChatDao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatJpaRepository extends JpaRepository<ChatDao, UUID> {


    @Override
    @EntityGraph(attributePaths = "participants")
    Optional<ChatDao> findById(UUID id);


    @EntityGraph(attributePaths = "participants")
    @Query("select c from ChatDao c where exists "
            + "(select 1 from ParticipantDao p where p.chat = c and p.userId = :userId)")
    List<ChatDao> findAllByParticipantUserId(@Param("userId") UUID userId);

    /** Membership check for guards that must not pay for loading the chat. */
    @Query("select count(p) > 0 from ParticipantDao p where p.chat.id = :chatId and p.userId = :userId")
    boolean isParticipant(@Param("chatId") UUID chatId, @Param("userId") UUID userId);
}
