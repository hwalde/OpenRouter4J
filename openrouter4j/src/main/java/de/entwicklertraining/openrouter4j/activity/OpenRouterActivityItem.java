package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * A typed view of one activity row of GET /activity ({@code data[]}):
 * the per-day, per-model, per-endpoint usage aggregation.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterActivityItem {

    private final JSONObject json;

    OpenRouterActivityItem(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw activity row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code date} - the day of the activity (YYYY-MM-DD).
     *
     * @return the value, or {@code null} when absent
     */
    public String date() {
        return json.optString("date", null);
    }

    /**
     * JSON path: {@code model} - the model slug (e.g. {@code openai/gpt-4.1}).
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        return json.optString("model", null);
    }

    /**
     * JSON path: {@code model_permaslug} - the pinned permaslug of the model.
     *
     * @return the value, or {@code null} when absent
     */
    public String modelPermaslug() {
        return json.optString("model_permaslug", null);
    }

    /**
     * JSON path: {@code endpoint_id} - unique identifier of the endpoint.
     *
     * @return the value, or {@code null} when absent
     */
    public String endpointId() {
        return json.optString("endpoint_id", null);
    }

    /**
     * JSON path: {@code provider_name} - the provider serving this endpoint.
     *
     * @return the value, or {@code null} when absent
     */
    public String providerName() {
        return json.optString("provider_name", null);
    }

    /**
     * JSON path: {@code usage} - total cost in USD (OpenRouter credits spent).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usage() {
        if (!json.has("usage") || json.isNull("usage")) {
            return null;
        }
        return json.optDouble("usage");
    }

    /**
     * JSON path: {@code byok_usage_inference} - BYOK inference cost in USD
     * (external credits spent).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageInference() {
        if (!json.has("byok_usage_inference") || json.isNull("byok_usage_inference")) {
            return null;
        }
        return json.optDouble("byok_usage_inference");
    }

    /**
     * JSON path: {@code requests} - number of requests made.
     *
     * @return the value, or {@code null} when absent
     */
    public Long requests() {
        if (!json.has("requests") || json.isNull("requests")) {
            return null;
        }
        return json.optLong("requests");
    }

    /**
     * JSON path: {@code prompt_tokens} - total prompt tokens used.
     *
     * @return the value, or {@code null} when absent
     */
    public Long promptTokens() {
        if (!json.has("prompt_tokens") || json.isNull("prompt_tokens")) {
            return null;
        }
        return json.optLong("prompt_tokens");
    }

    /**
     * JSON path: {@code completion_tokens} - total completion tokens generated.
     *
     * @return the value, or {@code null} when absent
     */
    public Long completionTokens() {
        if (!json.has("completion_tokens") || json.isNull("completion_tokens")) {
            return null;
        }
        return json.optLong("completion_tokens");
    }

    /**
     * JSON path: {@code reasoning_tokens} - total reasoning tokens used.
     *
     * @return the value, or {@code null} when absent
     */
    public Long reasoningTokens() {
        if (!json.has("reasoning_tokens") || json.isNull("reasoning_tokens")) {
            return null;
        }
        return json.optLong("reasoning_tokens");
    }

    /**
     * JSON path: {@code workspace_id} - the workspace this activity is
     * attributed to; only present when the request used {@code group_by=workspace}.
     *
     * @return the value, or {@code null} when absent
     */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }
}
