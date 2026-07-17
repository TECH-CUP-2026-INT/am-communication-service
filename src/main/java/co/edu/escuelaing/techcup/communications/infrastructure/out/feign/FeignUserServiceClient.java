package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import co.edu.escuelaing.techcup.communications.infrastructure.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.domain.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.domain.service.ports.out.UserServiceClient;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Calls cc-users-players-service's {@code GET /internal/players/{id}/exists}
 * (InternalPlayerController), the service-to-service existence check it built for exactly this
 * purpose. cc-identity-service owns auth/credentials only and has no equivalent lookup route.
 * The check can still be disabled via {@code integrations.user-service.existence-check-enabled=false}
 * if that dependency ever becomes unreachable.
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
            return feignClient.exists(userId).exists();
        } catch (FeignException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
