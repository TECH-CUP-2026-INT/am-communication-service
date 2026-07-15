package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.adapter;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper.ModeratorActionPersistenceMapper;

import co.edu.escuelaing.techcup.communications.domain.model.ModeratorAction;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ModeratorActionJpaRepository;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.ModeratorActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ModeratorActionRepositoryAdapter implements ModeratorActionRepository {

    private final ModeratorActionJpaRepository jpaRepository;
    private final ModeratorActionPersistenceMapper mapper;

    @Override
    public ModeratorAction save(ModeratorAction action) {
        return mapper.toDomain(jpaRepository.save(mapper.toDao(action)));
    }
}
