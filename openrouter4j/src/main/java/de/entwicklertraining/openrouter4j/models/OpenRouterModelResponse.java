package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /model/{author}/{slug}: one catalog entry.
 */
public final class OpenRouterModelResponse extends OpenRouterResponse<OpenRouterModelRequest> {

    OpenRouterModelResponse(JSONObject json, OpenRouterModelRequest request) {
        super(json, request);
    }

    /**
     * @return the catalog entry as a typed {@link OpenRouterModel} view, or
     *         {@code null} when {@code data} is absent or not an object
     */
    public OpenRouterModel model() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterModel(data) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
