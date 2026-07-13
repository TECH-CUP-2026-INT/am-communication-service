package co.edu.escuelaing.techcup.communications.repository;

import co.edu.escuelaing.techcup.communications.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqRepository extends JpaRepository<Faq, UUID> {

    List<Faq> findAllByOrderByCreatedAtAsc();
}
