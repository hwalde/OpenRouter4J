package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of one cost-per-session cell of GET /datasets/session-cost
 * ({@code data[]}).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterSessionCostRow {

    private final JSONObject json;

    OpenRouterSessionCostRow(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code app_slug} - the published harness slug.
     *
     * @return the value, or {@code null} when absent
     */
    public String appSlug() {
        return json.optString("app_slug", null);
    }

    /**
     * JSON path: {@code app_name} - the harness display name.
     *
     * @return the value, or {@code null} when absent
     */
    public String appName() {
        return json.optString("app_name", null);
    }

    /**
     * JSON path: {@code model_permaslug} - the model permaslug.
     *
     * @return the value, or {@code null} when absent
     */
    public String modelPermaslug() {
        return json.optString("model_permaslug", null);
    }

    /**
     * JSON path: {@code turn_range} - the session turn-range bucket
     * (e.g. {@code 10-49-turns}).
     *
     * @return the value, or {@code null} when absent
     */
    public String turnRange() {
        return json.optString("turn_range", null);
    }

    /**
     * JSON path: {@code median_session_cost_usd} - the median per-session
     * USD spend.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double medianSessionCostUsd() {
        if (!json.has("median_session_cost_usd") || json.isNull("median_session_cost_usd")) {
            return null;
        }
        return json.optDouble("median_session_cost_usd");
    }
}
