package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiResponse;
import org.json.JSONObject;

/**
 * Abstract base for OpenRouter-specific responses; extends
 * {@code ApiResponse<OpenRouterRequest<?>>} from the api-base library.
 *
 * @param <T> the request type this response belongs to
 */
public abstract class OpenRouterResponse<T extends OpenRouterRequest<?>> extends ApiResponse<T> {

    /** The parsed response body. Subclasses read their accessors out of this. */
    protected final JSONObject json;

    protected OpenRouterResponse(JSONObject json, T request) {
        super(request);
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }
}
