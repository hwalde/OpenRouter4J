package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of one macro category of GET /classifications/task
 * ({@code data.macro_categories[]}): Code, Data, Agent, General with
 * aggregate shares.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterMacroCategory {

    private final JSONObject json;

    OpenRouterMacroCategory(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw macro-category row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code key} - the macro category key (e.g. {@code code}).
     *
     * @return the value, or {@code null} when absent
     */
    public String key() {
        return json.optString("key", null);
    }

    /**
     * JSON path: {@code label} - the human-readable label (e.g. {@code Code}).
     *
     * @return the value, or {@code null} when absent
     */
    public String label() {
        return json.optString("label", null);
    }

    /**
     * JSON path: {@code usage_share} - aggregate request share (fraction 0..1).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageShare() {
        if (!json.has("usage_share") || json.isNull("usage_share")) {
            return null;
        }
        return json.optDouble("usage_share");
    }

    /**
     * JSON path: {@code token_share} - aggregate token share (fraction 0..1).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double tokenShare() {
        if (!json.has("token_share") || json.isNull("token_share")) {
            return null;
        }
        return json.optDouble("token_share");
    }
}
