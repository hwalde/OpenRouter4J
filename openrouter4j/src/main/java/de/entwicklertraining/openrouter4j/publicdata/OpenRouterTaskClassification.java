package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one classification of GET /classifications/task
 * ({@code data.classifications[]}): one task classification with its market
 * shares and its top models.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterTaskClassification {

    private final JSONObject json;

    OpenRouterTaskClassification(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw classification row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code tag} - the classification tag (e.g. {@code code:general_impl}).
     *
     * @return the value, or {@code null} when absent
     */
    public String tag() {
        return json.optString("tag", null);
    }

    /**
     * JSON path: {@code display_name} - the human-readable name
     * (e.g. {@code Code Generation}).
     *
     * @return the value, or {@code null} when absent
     */
    public String displayName() {
        return json.optString("display_name", null);
    }

    /**
     * JSON path: {@code macro_category} - the macro category key
     * (e.g. {@code code}).
     *
     * @return the value, or {@code null} when absent
     */
    public String macroCategory() {
        return json.optString("macro_category", null);
    }

    /**
     * JSON path: {@code usage_share} - share of classified sampled requests
     * (fraction 0..1).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageShare() {
        return optDouble("usage_share");
    }

    /**
     * JSON path: {@code token_share} - share of classified sampled token
     * volume (fraction 0..1).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double tokenShare() {
        return optDouble("token_share");
    }

    /**
     * JSON path: {@code category_usage_share} - share within the macro
     * category (requests).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double categoryUsageShare() {
        return optDouble("category_usage_share");
    }

    /**
     * JSON path: {@code category_token_share} - share within the macro
     * category (tokens).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double categoryTokenShare() {
        return optDouble("category_token_share");
    }

    /**
     * JSON path: {@code models[]} - the top models by request volume within
     * this classification.
     *
     * @return the model entries as typed views, empty when absent
     */
    public List<OpenRouterTaskClassificationModel> models() {
        List<OpenRouterTaskClassificationModel> result = new ArrayList<>();
        try {
            JSONArray models = json.optJSONArray("models");
            if (models != null) {
                for (int i = 0; i < models.length(); i++) {
                    JSONObject entry = models.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterTaskClassificationModel(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    private Double optDouble(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optDouble(key);
    }
}
