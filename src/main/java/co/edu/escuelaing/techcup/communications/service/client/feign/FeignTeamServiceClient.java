package co.edu.escuelaing.techcup.communications.service.client.feign;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.TeamServiceClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * cc-teams-service is currently an empty skeleton with no endpoints. Until it exposes a team
 * lookup, the existence check can be disabled via
 * {@code integrations.team-service.existence-check-enabled=false} so chat creation isn't
 * blocked on a dependency that doesn't exist yet.
 */
@Slf4j
@Component
public class FeignTeamServiceClient implements TeamServiceClient {

    private static final String SERVICE = "team service";

    private final TeamServiceFeignClient feignClient;
    private final boolean existenceCheckEnabled;

    public FeignTeamServiceClient(TeamServiceFeignClient feignClient, IntegrationProperties properties) {
        this.feignClient = feignClient;
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
            feignClient.getTeam(teamId);
            return true;
        } catch (FeignException.NotFound ex) {
            return false;
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
