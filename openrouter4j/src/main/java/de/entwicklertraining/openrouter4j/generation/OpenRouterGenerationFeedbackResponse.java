package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /generation/feedback: confirmation that the feedback was
 * recorded.
 */
public final class OpenRouterGenerationFeedbackResponse extends OpenRouterResponse<OpenRouterGenerationFeedbackRequest> {

    OpenRouterGenerationFeedbackResponse(JSONObject json, OpenRouterGenerationFeedbackRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.success} - whether the feedback was recorded.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean success() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data == null || !data.has("success") || data.isNull("success")) {
                return null;
            }
            return data.optBoolean("success");
        } catch (Exception e) {
            return null;
        }
    }
}
