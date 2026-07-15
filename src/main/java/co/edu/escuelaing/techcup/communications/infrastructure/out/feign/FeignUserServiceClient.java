package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * cc-identity-service does not yet expose a user lookup endpoint. Until it does, the existence
 * check can be disabled via {@code integrations.user-service.existence-check-enabled=false} so
 * chat creation isn't blocked on a dependency that doesn't exist yet.
 */
@Slf4j
@Component
public class FeignUserServiceClient implements UserServiceClient {

    private static final String SERVICE = "user service";

    private final UserServiceFeignClient feignClient;
    private final boolean existenceCheckEnabled;

    public FeignUserServiceClient(UserServiceFeignClient feignClient, IntegrationProperties properties) {
        this.feignClient = feignClient;
        this.existenceCheckEnabled = properties.userService().existenceCheckEnabled();
        if (!existenceCheckEnabled) {
            log.warn("User existence checks are disabled: every userId is treated as existing");
        }
    }

    @Override
    public boolean exists(UUID userId) {
        if (!existenceCheckEnabled) {
            return true;
        }
        try {
            feignClient.getUser(userId);
            return true;
        } catch (FeignException.NotFound ex) {
            return false;
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
