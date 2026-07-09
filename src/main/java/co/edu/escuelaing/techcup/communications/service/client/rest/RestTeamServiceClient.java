package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;
import co.edu.escuelaing.techcup.communications.exception.IntegrationException;
import co.edu.escuelaing.techcup.communications.service.client.TeamServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RestTeamServiceClient implements TeamServiceClient {

    private static final String SERVICE = "team service";

    private final RestClient restClient;

    public RestTeamServiceClient(RestClient.Builder builder, IntegrationProperties properties) {
        this.restClient = builder.baseUrl(properties.teamService().baseUrl()).build();
    }

    @Override
    public boolean exists(UUID teamId) {
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
