package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONObject;

/**
 * A typed view of one metric of GET /analytics/meta
 * ({@code data.metrics[]}): a metric the analytics query engine accepts for
 * {@code metrics(...)}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterAnalyticsMetric {

    private final JSONObject json;

    OpenRouterAnalyticsMetric(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw metric row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the metric identifier used in query requests
     * (e.g. {@code request_count}).
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code display_label} - the human-readable label
     * (e.g. {@code Request Count}).
     *
     * @return the value, or {@code null} when absent
     */
    public String displayLabel() {
        return json.optString("display_label", null);
    }

    /**
     * JSON path: {@code display_format} - the display format of the metric
     * (e.g. {@code number}).
     *
     * @return the value, or {@code null} when absent
     */
    public String displayFormat() {
        return json.optString("display_format", null);
    }

    /**
     * JSON path: {@code is_rate} - whether the metric is a rate.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isRate() {
        if (!json.has("is_rate") || json.isNull("is_rate")) {
            return null;
        }
        return json.optBoolean("is_rate");
    }
}
