package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /private-endpoints (201), POST
 * /private-endpoints/{id}/activate, /enable and /disable (schema
 * {@code ManagedPrivateEndpointResponse}, {@code {"data": {...}}}): the
 * managed endpoint shape - summary fields plus the data-policy attestations
 * and the completion cap.
 *
 * <p>Trap: this shape does not echo {@code declared_parameters} or
 * {@code tested_parameters} - read those with GET /private-endpoints/{id}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterManagedPrivateEndpointResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterManagedPrivateEndpointResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the managed endpoint.
     *
     * @return the endpoint as a typed view, or {@code null} when {@code data}
     *         is absent or not an object
     */
    public OpenRouterManagedPrivateEndpoint data() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterManagedPrivateEndpoint(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
