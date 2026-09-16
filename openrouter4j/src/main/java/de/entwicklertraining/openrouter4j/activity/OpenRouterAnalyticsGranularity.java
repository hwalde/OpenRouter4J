package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONObject;

/**
 * A typed view of one granularity of GET /analytics/meta
 * ({@code data.granularities[]}): a time grain the analytics query engine
 * accepts for {@code granularity(...)}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterAnalyticsGranularity {

    private final JSONObject json;

    OpenRouterAnalyticsGranularity(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw granularity row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the granularity identifier (e.g. {@code day}).
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code display_label} - the human-readable label
     * (e.g. {@code Day}).
     *
     * @return the value, or {@code null} when absent
     */
    public String displayLabel() {
        return json.optString("display_label", null);
    }
}
