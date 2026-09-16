package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of one model entry of GET /classifications/task
 * ({@code data.classifications[].models[]}): a top model within a
 * classification.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterTaskClassificationModel {

    private final JSONObject json;

    OpenRouterTaskClassificationModel(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw model entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the model id (e.g. {@code openai/gpt-4.1-mini}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code tag_usage_share} - the within-tag usage share.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double tagUsageShare() {
        return optDouble("tag_usage_share");
    }

    /**
     * JSON path: {@code tag_token_share} - the within-tag token share.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double tagTokenShare() {
        return optDouble("tag_token_share");
    }

    private Double optDouble(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optDouble(key);
    }
}
