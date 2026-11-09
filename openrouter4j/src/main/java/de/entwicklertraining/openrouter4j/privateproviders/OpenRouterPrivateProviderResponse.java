package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the private-provider routes that answer with a
 * single provider wrapped in {@code data} ({@code {"data": {...}}}): the
 * create (201), get and update routes.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateProviderResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateProviderResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the provider.
     *
     * @return the provider as a typed view, or {@code null} when {@code data}
     *         is absent or not an object
     */
    public OpenRouterPrivateProvider data() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterPrivateProvider(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
