package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.UserServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RestUserServiceClient implements UserServiceClient {

    private static final String SERVICE = "user service";

    private final RestClient restClient;

    public RestUserServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.userService().baseUrl()).build();
    }

    @Override
    public boolean exists(UUID userId) {
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
