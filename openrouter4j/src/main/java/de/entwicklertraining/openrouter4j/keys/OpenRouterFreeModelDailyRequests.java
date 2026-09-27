package de.entwicklertraining.openrouter4j.keys;

import org.json.JSONObject;

/**
 * A typed view of {@code data.free_model_daily_requests} on {@code GET /key}
 * (only on the current-key surface, not on the {@code GET /keys} list items):
 * the free-model ({@code :free} variant) daily request quota of the account
 * that owns the key.
 *
 * <p>Traps: the counter resets at UTC midnight, and accounts and endpoints
 * that are exempt from free-model limits - plus BYOK requests - are not gated
 * by it at all, so {@link #remaining()} can report the tier policy rather
 * than a real budget.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterFreeModelDailyRequests {

    private final JSONObject json;

    OpenRouterFreeModelDailyRequests(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code free_model_daily_requests} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code limit} - the daily free-model request allowance.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer limit() {
        return optInt("limit");
    }

    /**
     * JSON path: {@code used} - requests already spent in the current window.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer used() {
        return optInt("used");
    }

    /**
     * JSON path: {@code remaining} - requests left in the current window.
     * This can report the tier policy rather than a real budget (see the
     * class javadoc).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer remaining() {
        return optInt("remaining");
    }

    private Integer optInt(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.get(key);
        return value instanceof Number number ? number.intValue() : null;
    }
}
