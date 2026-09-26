package de.entwicklertraining.openrouter4j.batches;

import org.json.JSONObject;

/**
 * A typed view of the {@code deletion} object of a DELETE
 * {@code /api/v1/batches/:id} answer: the outcome on OpenRouter's side and
 * the outcome of the upstream provider cleanup.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBatchDeletion {

    private final JSONObject json;

    OpenRouterBatchDeletion(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code deletion} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code openrouter} - always {@code deleted} on a
     * {@code 200} answer.
     *
     * @return the value, or {@code null} when absent
     */
    public String openrouter() {
        return json.optString("openrouter", null);
    }

    /**
     * JSON path: {@code upstream.provider} - the provider that ran the batch
     * (e.g. {@code Anthropic}); the {@code upstream} object is omitted
     * entirely when no provider was assigned.
     *
     * @return the value, or {@code null} when absent
     */
    public String upstreamProvider() {
        JSONObject upstream = upstream();
        return upstream == null ? null : upstream.optString("provider", null);
    }

    /**
     * JSON path: {@code upstream.status} - {@code deleted} when native batch
     * deletion is supported (Anthropic, Fireworks, Google AI Studio, Google
     * Vertex, Mistral), {@code unsupported} otherwise, or {@code
     * not_applicable} when no upstream batch was created.
     *
     * @return the value, or {@code null} when absent
     */
    public String upstreamStatus() {
        JSONObject upstream = upstream();
        return upstream == null ? null : upstream.optString("status", null);
    }

    /**
     * JSON path: {@code upstream} - the raw upstream cleanup object.
     *
     * @return the object, or {@code null} when absent
     */
    public JSONObject upstream() {
        return json.optJSONObject("upstream");
    }
}
