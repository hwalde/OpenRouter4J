package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The response of a {@link OpenRouterScimGroupMappingDeleteRequest}:
 * {@code {"deleted": true}}.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterScimGroupMappingDeleteResponse extends OpenRouterResponse<OpenRouterScimGroupMappingDeleteRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterScimGroupMappingDeleteResponse(JSONObject json, OpenRouterScimGroupMappingDeleteRequest request) {
        super(json, request);
    }

    /** @return the JSON field {@code deleted}, or {@code null} when absent */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        Object value = json.get("deleted");
        return value instanceof Boolean b ? b : null;
    }
}
