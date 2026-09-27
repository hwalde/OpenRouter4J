package de.entwicklertraining.openrouter4j.models;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code pricing.overrides} on a model
 * (GET /models, GET /model/{author}/{slug}): a date- and size-scoped price
 * table that overrides the listed base prices while it applies.
 *
 * <p>Trap: a price lookup that ignores {@code overrides} is wrong for any
 * model with a time- or size-scoped price table - a cost estimate must
 * consult them (matched by prompt size against {@link #minPromptTokens()} and
 * by UTC day/time window against {@link #utcDays()} / {@link #utcStart()} /
 * {@link #utcEnd()}).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor. The
 * prices stay decimal-as-string, the wire convention - do not parse to
 * double.
 */
public final class OpenRouterPricingOverride {

    private final JSONObject json;

    OpenRouterPricingOverride(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw override object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code min_prompt_tokens} - the prompt size this override
     * applies from (prompts at or above this token count).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double minPromptTokens() {
        return optDouble("min_prompt_tokens");
    }

    /**
     * JSON path: {@code utc_days} - the UTC days of the week this override
     * applies on (numeric day values as sent by the API; non-numeric entries
     * are dropped).
     *
     * @return the days, empty when absent or not an array
     */
    public List<Long> utcDays() {
        List<Long> result = new ArrayList<>();
        JSONArray days = json.optJSONArray("utc_days");
        if (days == null) {
            return result;
        }
        for (int i = 0; i < days.length(); i++) {
            Object value = days.opt(i);
            if (value instanceof Number number) {
                result.add(number.longValue());
            }
        }
        return result;
    }

    /**
     * JSON path: {@code utc_start} - start of the UTC time window this
     * override applies in.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long utcStart() {
        return optLong("utc_start");
    }

    /**
     * JSON path: {@code utc_end} - end of the UTC time window this override
     * applies in.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long utcEnd() {
        return optLong("utc_end");
    }

    /**
     * JSON path: {@code prompt} - overridden USD per prompt (input) token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String prompt() {
        return optPrice("prompt");
    }

    /**
     * JSON path: {@code completion} - overridden USD per completion (output)
     * token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String completion() {
        return optPrice("completion");
    }

    /**
     * JSON path: {@code image} - overridden USD per input image.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String image() {
        return optPrice("image");
    }

    /**
     * JSON path: {@code audio} - overridden USD per audio input token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String audio() {
        return optPrice("audio");
    }

    /**
     * JSON path: {@code input_cache_read} - overridden USD per cached input
     * token (read).
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String inputCacheRead() {
        return optPrice("input_cache_read");
    }

    /**
     * JSON path: {@code input_cache_write} - overridden USD per prompt-cache
     * write token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String inputCacheWrite() {
        return optPrice("input_cache_write");
    }

    /**
     * JSON path: {@code input_cache_write_1h} - overridden USD per 1-hour
     * TTL prompt-cache write token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String inputCacheWrite1h() {
        return optPrice("input_cache_write_1h");
    }

    /**
     * JSON path: {@code input_audio_cache} - overridden USD per cached input
     * audio token.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String inputAudioCache() {
        return optPrice("input_audio_cache");
    }

    private String optPrice(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.opt(key);
        return value instanceof String text ? text : null;
    }

    private Long optLong(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.opt(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private Double optDouble(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.opt(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
