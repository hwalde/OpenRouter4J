package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the intern create, get and update endpoints
 * (POST /interns, GET /interns/{internId}, PATCH /interns/{internId}): the
 * public lifecycle state and settings of the intern.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterInternResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterInternResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * @return the intern view, or {@code null} when the body carries no
     *         {@code id} (a malformed body)
     */
    public OpenRouterIntern intern() {
        if (json == null || !json.has("id")) {
            return null;
        }
        return new OpenRouterIntern(json);
    }
}
