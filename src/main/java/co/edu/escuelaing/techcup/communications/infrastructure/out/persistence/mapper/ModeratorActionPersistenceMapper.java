package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.ModeratorAction;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.ModeratorActionDao;
import org.mapstruct.Mapper;

/** Maps {@link ModeratorActionDao} to/from {@link ModeratorAction}. No associations, immutable. */
@Mapper(componentModel = "spring")
public interface ModeratorActionPersistenceMapper {

    default ModeratorAction toDomain(ModeratorActionDao dao) {
        return ModeratorAction.fromPersistence(dao.getId(), dao.getModeratorId(), dao.getTargetType(),
                dao.getTargetId(), dao.getActionType(), dao.getReason(), dao.getCreatedAt());
    }

    default ModeratorActionDao toDao(ModeratorAction action) {
        ModeratorActionDao dao = new ModeratorActionDao();
        dao.setId(action.getId());
        dao.setModeratorId(action.getModeratorId());
        dao.setTargetType(action.getTargetType());
        dao.setTargetId(action.getTargetId());
        dao.setActionType(action.getActionType());
        dao.setReason(action.getReason());
        dao.setCreatedAt(action.getCreatedAt());
        return dao;
    }
}
