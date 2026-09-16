package de.entwicklertraining.openrouter4j.workspace;

import org.json.JSONObject;

/**
 * A typed view of one workspace budget (GET /workspaces/{ref}/budgets, GET and
 * PUT /workspaces/{ref}/budgets/{interval}): the spending limit per interval.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterWorkspaceBudget {

    private final JSONObject json;

    OpenRouterWorkspaceBudget(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier of the budget.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code workspace_id} - id of the workspace the budget belongs to.
 */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }
    /**
 * JSON path: {@code limit_usd} - spending limit in USD for this interval.
 */
    public Double limitUsd() {
        if (!json.has("limit_usd") || json.isNull("limit_usd")) {
            return null;
        }
        return json.optDouble("limit_usd");
    }
    /**
 * JSON path: {@code reset_interval} - the interval at which spend resets
 * ({@code daily}, {@code weekly}, {@code monthly}), or {@code null} for a
 * lifetime (one-time) budget.
 */
    public String resetInterval() {
        return json.optString("reset_interval", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 timestamp of when the budget was
 * created.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
    /**
 * JSON path: {@code updated_at} - ISO 8601 timestamp of when the budget was last
 * updated.
 */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }
}
