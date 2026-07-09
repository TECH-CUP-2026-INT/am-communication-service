package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;

/** Builds {@link IntegrationProperties} where every microservice sits behind the same base URL. */
final class IntegrationTestProperties {

    private IntegrationTestProperties() {
    }

    static IntegrationProperties pointingAt(String baseUrl) {
        IntegrationProperties.Endpoint endpoint = new IntegrationProperties.Endpoint(baseUrl);
        return new IntegrationProperties(endpoint, endpoint, endpoint, endpoint);
    }
}
