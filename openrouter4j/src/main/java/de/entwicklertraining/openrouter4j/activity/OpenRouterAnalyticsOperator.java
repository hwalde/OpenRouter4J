package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONObject;

/**
 * A typed view of one filter operator of GET /analytics/meta
 * ({@code data.operators[]}): an operator the analytics query engine accepts
 * for {@code filter(field, operator, value)}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterAnalyticsOperator {

    private final JSONObject json;

    OpenRouterAnalyticsOperator(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw operator row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the operator identifier (e.g. {@code eq}).
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code value_type} - the value shape the operator takes
     * (e.g. {@code scalar}).
     *
     * @return the value, or {@code null} when absent
     */
    public String valueType() {
        return json.optString("value_type", null);
    }
}
