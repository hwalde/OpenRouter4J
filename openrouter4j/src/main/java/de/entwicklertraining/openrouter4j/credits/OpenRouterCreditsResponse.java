package de.entwicklertraining.openrouter4j.credits;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /credits: the total credits purchased and used on the
 * authenticated account, in USD.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention: a field that is absent (or a malformed body) yields
 * {@code null} instead of an exception. Use {@link #getJson()} to inspect the
 * raw response.
 */
public final class OpenRouterCreditsResponse extends OpenRouterResponse<OpenRouterCreditsRequest> {

    OpenRouterCreditsResponse(JSONObject json, OpenRouterCreditsRequest request) {
        super(json, request);
    }

    /**
     * @return the raw {@code data} object of the response, or {@code null} when absent
     */
    public JSONObject data() {
        try {
            return json.optJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.total_credits} - total credits purchased, USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double totalCredits() {
        try {
            return data() != null ? data().optDoubleObject("total_credits", null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.total_usage} - total credits used so far, USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double totalUsage() {
        try {
            return data() != null ? data().optDoubleObject("total_usage", null) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
