package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of one row of GET /datasets/rankings-daily
 * ({@code data[]}): one {@code (date, model_permaslug)} token total; the
 * aggregated row outside the top 50 uses the reserved permaslug {@code other}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterRankingsDailyRow {

    private final JSONObject json;

    OpenRouterRankingsDailyRow(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code date} - the day of the row (YYYY-MM-DD).
     *
     * @return the value, or {@code null} when absent
     */
    public String date() {
        return json.optString("date", null);
    }

    /**
     * JSON path: {@code model_permaslug} - the pinned permaslug of the model,
     * or {@code other} for the aggregated row outside the top 50.
     *
     * @return the value, or {@code null} when absent
     */
    public String modelPermaslug() {
        return json.optString("model_permaslug", null);
    }

    /**
     * JSON path: {@code total_tokens} - the total tokens for the day
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
     * @return whether this is the aggregated {@code other} row
     */
    public boolean isOther() {
        return "other".equals(modelPermaslug());
    }
}
