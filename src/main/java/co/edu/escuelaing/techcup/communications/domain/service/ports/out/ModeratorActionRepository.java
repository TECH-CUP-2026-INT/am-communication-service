package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import co.edu.escuelaing.techcup.communications.domain.model.ModeratorAction;

public interface ModeratorActionRepository {

    ModeratorAction save(ModeratorAction action);
}
