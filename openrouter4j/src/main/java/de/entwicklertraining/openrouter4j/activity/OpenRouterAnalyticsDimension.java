package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONObject;

/**
 * A typed view of one dimension of GET /analytics/meta
 * ({@code data.dimensions[]}): a dimension the analytics query engine accepts
 * for {@code dimensions(...)} and {@code filter(field, ...)}.
 *
 * <p>Trap carried over from the query endpoint: filters on enriched dimensions
 * must use the underlying <b>id</b> (permaslug for {@code model}, workspace
 * UUID for {@code workspace}), not the display label.
 */
public final class OpenRouterAnalyticsDimension {

    private final JSONObject json;

    OpenRouterAnalyticsDimension(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw dimension row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the dimension identifier used in query
     * requests (e.g. {@code model}).
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code display_label} - the human-readable label
     * (e.g. {@code Model}).
     *
     * @return the value, or {@code null} when absent
     */
    public String displayLabel() {
        return json.optString("display_label", null);
    }
}
