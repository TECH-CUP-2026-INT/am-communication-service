package co.edu.escuelaing.techcup.communications.service.client.rest;

import co.edu.escuelaing.techcup.communications.config.IntegrationProperties;

/** Builds {@link IntegrationProperties} where every microservice sits behind the same base URL. */
final class IntegrationTestProperties {

    private IntegrationTestProperties() {
    }

    static IntegrationProperties pointingAt(String baseUrl) {
        return pointingAt(baseUrl, true);
    }

    static IntegrationProperties pointingAt(String baseUrl, boolean existenceCheckEnabled) {
        IntegrationProperties.Endpoint endpoint = new IntegrationProperties.Endpoint(baseUrl, existenceCheckEnabled);
        return new IntegrationProperties(endpoint, endpoint, endpoint, endpoint);
    }
}
