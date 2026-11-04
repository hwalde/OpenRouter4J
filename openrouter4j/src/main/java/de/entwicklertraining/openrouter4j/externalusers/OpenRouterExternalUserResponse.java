package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the external-user routes that answer with a single
 * registration (schema {@code ExternalUserResponse},
 * {@code {"data": {...}}}): the create (201), get and update routes.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterExternalUserResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterExternalUserResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the registration.
     *
     * @return the registration as a typed view, or {@code null} when
     *         {@code data} is absent or not an object
     */
    public OpenRouterExternalUser data() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterExternalUser(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
