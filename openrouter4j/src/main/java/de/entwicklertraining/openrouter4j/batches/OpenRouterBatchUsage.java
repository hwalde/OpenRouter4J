package de.entwicklertraining.openrouter4j.batches;

import org.json.JSONObject;

/**
 * A typed view of the {@code usage} object of a completed batch. {@code
 * usage} is {@code null} before completion in every documented example;
 * list items carry usage only once the batch is done.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBatchUsage {

    private final JSONObject json;

    OpenRouterBatchUsage(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code usage} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code prompt_tokens} - the input tokens of the whole batch.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer promptTokens() {
        return optInteger("prompt_tokens");
    }

    /**
     * JSON path: {@code completion_tokens} - the output tokens of the whole
     * batch.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer completionTokens() {
        return optInteger("completion_tokens");
    }

    /**
     * JSON path: {@code total_tokens} - the sum of input and output tokens.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer totalTokens() {
        return optInteger("total_tokens");
    }

    /**
     * JSON path: {@code cost} - the amount OpenRouter charges for the
     * completed batch. Batch inference is typically billed at ~50% of the
     * model's standard per-token pricing; for BYOK-routed batches this is
     * only the OpenRouter BYOK fee (the provider bills the inference).
     *
     * @return the value, or {@code null} when absent
     */
    public Double cost() {
        if (!json.has("cost") || json.isNull("cost")) {
            return null;
        }
        return json.optDouble("cost");
    }

    /**
     * JSON path: {@code is_byok} - {@code true} when the batch routed through
     * a provider key.
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean isByok() {
        if (!json.has("is_byok") || json.isNull("is_byok")) {
            return null;
        }
        return json.optBoolean("is_byok");
    }

    private Integer optInteger(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optInt(key);
    }
}
