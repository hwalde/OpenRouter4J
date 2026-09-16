package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of one app-ranking row of GET /datasets/app-rankings
 * ({@code data[]}): a public app with its rank and token volume.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterAppRanking {

    private final JSONObject json;

    OpenRouterAppRanking(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code rank} - the absolute rank (paging keeps it absolute:
     * the first row of {@code offset=50} is rank 51; filtering re-numbers
     * 1..N).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer rank() {
        if (!json.has("rank") || json.isNull("rank")) {
            return null;
        }
        return (int) json.optLong("rank");
    }

    /**
     * JSON path: {@code app_id} - the app identifier.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long appId() {
        if (!json.has("app_id") || json.isNull("app_id")) {
            return null;
        }
        return json.optLong("app_id");
    }

    /**
     * JSON path: {@code app_name} - the app display name.
     *
     * @return the value, or {@code null} when absent
     */
    public String appName() {
        return json.optString("app_name", null);
    }

    /**
     * JSON path: {@code total_tokens} - the token volume inside the window
     * ({@code prompt_tokens + completion_tokens}). The API sends this as a
     * string to preserve precision; use {@link #totalTokensAsLong()} for the
     * numeric form.
     *
     * @return the value, or {@code null} when absent
     */
    public String totalTokens() {
        return json.optString("total_tokens", null);
    }

    /**
     * JSON path: {@code total_tokens} parsed as a number.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalTokensAsLong() {
        if (!json.has("total_tokens") || json.isNull("total_tokens")) {
            return null;
        }
        return json.optLong("total_tokens");
    }

    /**
     * JSON path: {@code total_requests} - the request count inside the window.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalRequests() {
        if (!json.has("total_requests") || json.isNull("total_requests")) {
            return null;
        }
        return json.optLong("total_requests");
    }
}
