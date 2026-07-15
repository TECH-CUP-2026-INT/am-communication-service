package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa;

import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.FaqDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqJpaRepository extends JpaRepository<FaqDao, UUID> {

    List<FaqDao> findAllByOrderByCreatedAtAsc();
}
