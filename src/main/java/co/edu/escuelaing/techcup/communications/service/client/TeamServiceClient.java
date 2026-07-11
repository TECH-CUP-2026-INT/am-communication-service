package co.edu.escuelaing.techcup.communications.service.client;

import java.util.UUID;

/** Outbound port to the Teams microservice. */
public interface TeamServiceClient {

    boolean exists(UUID teamId);
}
