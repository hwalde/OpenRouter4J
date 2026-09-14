package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /models/count: the number of models matching the filter.
 */
public final class OpenRouterModelsCountResponse extends OpenRouterResponse<OpenRouterModelsCountRequest> {

    OpenRouterModelsCountResponse(JSONObject json, OpenRouterModelsCountRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.count} - number of models matching the filter.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Long count() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data == null || !data.has("count") || data.isNull("count")) {
                return null;
            }
            return data.optLong("count");
        } catch (Exception e) {
            return null;
        }
    }
}
