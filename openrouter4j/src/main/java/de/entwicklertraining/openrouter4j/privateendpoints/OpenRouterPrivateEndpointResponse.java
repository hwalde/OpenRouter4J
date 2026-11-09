package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /private-endpoints/{id} and PATCH
 * /private-endpoints/{id} (schema {@code PrivateEndpointResponse},
 * {@code {"data": {...}}}): the whole endpoint, settings and
 * {@code tested_parameters} included.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateEndpointResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateEndpointResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the endpoint.
     *
     * @return the endpoint as a typed view, or {@code null} when {@code data}
     *         is absent or not an object
     */
    public OpenRouterPrivateEndpoint data() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterPrivateEndpoint(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
