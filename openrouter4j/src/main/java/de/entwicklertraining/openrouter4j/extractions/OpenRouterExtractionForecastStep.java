package de.entwicklertraining.openrouter4j.extractions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A typed view of one step of the {@code steps} per-step forecast series of
 * a {@code forecast} answer (schema {@code ExtractionForecastStep}) - the
 * extracted {@code value} for that step plus the distribution statistics of
 * the same shape an {@code estimate} answer carries.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterExtractionForecastStep {

    private final JSONObject json;

    OpenRouterExtractionForecastStep(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw step object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code value} - the extracted value of this step (number
     * or string on the wire - money values travel as strings), or
     * {@code null} when the state does not determine it.
     *
     * @return the value as {@link Number} or {@link String}, or
     *         {@code null} when absent, JSON null or neither
     */
    public Object value() {
        return scalar("value");
    }

    /**
     * JSON path: {@code mean} - the mean of this step's distribution
     * (number or string on the wire).
     *
     * @return the mean as {@link Number} or {@link String}, or {@code null}
     *         when absent or neither
     */
    public Object mean() {
        return scalar("mean");
    }

    /**
     * JSON path: {@code median} - the median of this step's distribution
     * (number or string on the wire).
     *
     * @return the median as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object median() {
        return scalar("median");
    }

    /**
     * JSON path: {@code p10} - the 10th percentile of this step's
     * distribution (number or string on the wire).
     *
     * @return the percentile as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object p10() {
        return scalar("p10");
    }

    /**
     * JSON path: {@code p90} - the 90th percentile of this step's
     * distribution (number or string on the wire).
     *
     * @return the percentile as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object p90() {
        return scalar("p90");
    }

    /**
     * JSON path: {@code p_above} - the probability mass above {@code max}
     * for this step.
     *
     * @return the mass, or {@code null} when absent or not a number
     */
    public Double pAbove() {
        try {
            Object value = json.opt("p_above");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code p_below} - the probability mass below {@code min}
     * for this step.
     *
     * @return the mass, or {@code null} when absent or not a number
     */
    public Double pBelow() {
        try {
            Object value = json.opt("p_below");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code quantiles} - the 99-percentile table of this step's
     * distribution (present when the provider is asked for quantiles). Raw
     * rows as number or string.
     *
     * @return the quantile rows, empty when absent or not an array
     */
    public List<Object> quantiles() {
        List<Object> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("quantiles");
            if (arr == null) {
                return List.of();
            }
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.opt(i);
                result.add(value == null || JSONObject.NULL.equals(value) ? null : value);
            }
        } catch (Exception e) {
            // swallow: keep whatever was parsed before the failure
        }
        return Collections.unmodifiableList(result);
    }

    private Object scalar(String key) {
        try {
            Object value = json.opt(key);
            return value instanceof Number || value instanceof String ? value : null;
        } catch (Exception e) {
            return null;
        }
    }
}
