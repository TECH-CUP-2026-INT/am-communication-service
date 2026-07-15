package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.FaqPersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.FaqJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class FaqRepositoryAdapter implements FaqRepository {

    private final FaqJpaRepository jpaRepository;
    private final FaqPersistenceMapper mapper;

    @Override
    public List<Faq> findAllByOrderByCreatedAtAsc() {
        return jpaRepository.findAllByOrderByCreatedAtAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Faq save(Faq faq) {
        return mapper.toDomain(jpaRepository.save(mapper.toDao(faq)));
    }

    @Override
    public Optional<Faq> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void delete(Faq faq) {
        jpaRepository.deleteById(faq.getId());
    }
}
