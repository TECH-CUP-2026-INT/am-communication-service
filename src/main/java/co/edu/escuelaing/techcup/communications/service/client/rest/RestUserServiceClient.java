package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * cc-identity-service does not yet expose a user lookup endpoint. Until it does, the existence
 * check can be disabled via {@code integrations.user-service.existence-check-enabled=false} so
 * chat creation isn't blocked on a dependency that doesn't exist yet.
 */
@Slf4j
@Component
public class RestUserServiceClient implements UserServiceClient {

    private static final String SERVICE = "user service";

    private final RestClient restClient;
    private final boolean existenceCheckEnabled;

    public RestUserServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.userService().baseUrl()).build();
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
            restClient.get().uri("/users/{id}", userId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            throw new IntegrationException(SERVICE, ex);
        }
    }
}
