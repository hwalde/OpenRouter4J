package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /private-endpoints/{id} (schema
 * {@code DeletePrivateEndpointResponse}): confirmation carrying the deleted
 * {@code id}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateEndpointDeleteResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateEndpointDeleteResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.id} - the id (UUID) of the deleted endpoint.
     *
     * @return the id, or {@code null} when absent or not a string
     */
    public String id() {
        try {
            JSONObject data = json.optJSONObject("data");
            Object value = data != null ? data.opt("id") : null;
            return value instanceof String text ? text : null;
        } catch (Exception e) {
            return null;
        }
    }
}
