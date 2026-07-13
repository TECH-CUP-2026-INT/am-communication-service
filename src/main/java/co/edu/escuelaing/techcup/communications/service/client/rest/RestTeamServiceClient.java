package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.TeamServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * cc-teams-service is currently an empty skeleton with no endpoints. Until it exposes a team
 * lookup, the existence check can be disabled via
 * {@code integrations.team-service.existence-check-enabled=false} so chat creation isn't
 * blocked on a dependency that doesn't exist yet.
 */
@Slf4j
@Component
public class RestTeamServiceClient implements TeamServiceClient {

    private static final String SERVICE = "team service";

    private final RestClient restClient;
    private final boolean existenceCheckEnabled;

    public RestTeamServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.teamService().baseUrl()).build();
        this.existenceCheckEnabled = properties.teamService().existenceCheckEnabled();
        if (!existenceCheckEnabled) {
            log.warn("Team existence checks are disabled: every teamId is treated as existing");
        }
    }

    @Override
    public boolean exists(UUID teamId) {
        if (!existenceCheckEnabled) {
            return true;
        }
        try {
            restClient.get().uri("/teams/{id}", teamId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
