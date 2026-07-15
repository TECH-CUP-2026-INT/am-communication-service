package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ModeratorActionDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModeratorActionJpaRepository extends JpaRepository<ModeratorActionDao, UUID> {

    List<ModeratorActionDao> findByTargetId(UUID targetId);

    List<ModeratorActionDao> findByModeratorId(UUID moderatorId);
}
