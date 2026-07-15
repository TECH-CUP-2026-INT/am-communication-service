package co.edu.escuelaing.techcup.communications.domain.service.ports.out;

import java.util.UUID;

/**
 * Outbound port to the Users microservice. This service never reads another
 * microservice's database.
 */
public interface UserServiceClient {

    boolean exists(UUID userId);
}
