package de.entwicklertraining.openrouter4j.extractions;

import org.json.JSONObject;

/**
 * A typed view of the {@code usage} object of an Extractions response
 * (schema {@code ExtractionsUsage}): the required {@code input_tokens} and
 * {@code output_tokens}, plus the optional {@code cost}, {@code image_tokens},
 * {@code passes} (the number of upstream passes when the fields did not fit
 * in one), {@code schema_tokens} and {@code state_tokens}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterExtractionsUsage {

    private final JSONObject json;

    OpenRouterExtractionsUsage(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code usage} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code input_tokens} - the input tokens of the request.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer inputTokens() {
        return integerOf("input_tokens");
    }

    /**
     * JSON path: {@code output_tokens} - the output tokens of the request.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer outputTokens() {
        return integerOf("output_tokens");
    }

    /**
     * JSON path: {@code cost} - the amount OpenRouter charges for the
     * request in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double cost() {
        try {
            Object value = json.opt("cost");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code image_tokens} - the tokens of the image assets.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer imageTokens() {
        return integerOf("image_tokens");
    }

    /**
     * JSON path: {@code passes} - the number of upstream passes when the
     * fields did not fit in one.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer passes() {
        return integerOf("passes");
    }

    /**
     * JSON path: {@code schema_tokens} - the tokens of the field schema.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer schemaTokens() {
        return integerOf("schema_tokens");
    }

    /**
     * JSON path: {@code state_tokens} - the tokens of the state text.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer stateTokens() {
        return integerOf("state_tokens");
    }

    private Integer integerOf(String key) {
        try {
            Object value = json.opt(key);
            return value instanceof Number number ? number.intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
