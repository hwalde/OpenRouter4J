package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiResponse;
import org.json.JSONObject;

/**
 * Abstrakte Basis für OpenRouter-spezifische Responses,
 * erbt nun von ApiResponse<OpenRouterRequest<?>>.
 */
public abstract class OpenRouterResponse<T extends OpenRouterRequest<?>> extends ApiResponse<T> {

    protected final JSONObject json;  // In OpenRouterResponse wollen wir das JSON parsen/halten

    protected OpenRouterResponse(JSONObject json, T request) {
        super(request);
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }
}
