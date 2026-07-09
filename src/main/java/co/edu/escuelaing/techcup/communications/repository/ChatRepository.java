package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Chat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * Participants are fetched with the chat: responses are mapped once the transaction has
     * ended and open-in-view is disabled.
     */
    @Override
    @EntityGraph(attributePaths = "participants")
    Optional<Chat> findById(UUID id);

    /**
     * Membership is tested with an EXISTS subquery instead of a join on {@code participants},
     * so the fetched collection keeps every participant and not only the matching one.
     */
    @EntityGraph(attributePaths = "participants")
    @Query("select c from Chat c where exists "
            + "(select 1 from Participant p where p.chat = c and p.userId = :userId)")
    List<Chat> findAllByParticipantUserId(@Param("userId") UUID userId);
}
