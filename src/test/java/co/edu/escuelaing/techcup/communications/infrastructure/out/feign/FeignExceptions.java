package co.edu.escuelaing.techcup.communications.infrastructure.out.feign;

import feign.FeignException;
import feign.Request;
import feign.Response;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Builds a real {@link FeignException} (of the right status-specific subtype) for tests. */
final class FeignExceptions {

    private FeignExceptions() {
    }

    static FeignException withStatus(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://test", Map.of(), null,
                StandardCharsets.UTF_8, null);
        Response response = Response.builder()
                .status(status)
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("Client#method", response);
    }
}
