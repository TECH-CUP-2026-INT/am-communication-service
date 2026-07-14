package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.ModeratorAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModeratorActionRepository extends JpaRepository<ModeratorAction, UUID> {

    List<ModeratorAction> findByTargetId(UUID targetId);

    List<ModeratorAction> findByModeratorId(UUID moderatorId);
}
