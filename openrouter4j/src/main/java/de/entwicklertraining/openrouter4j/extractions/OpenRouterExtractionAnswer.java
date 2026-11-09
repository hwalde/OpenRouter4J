package de.entwicklertraining.openrouter4j.extractions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A typed view of one entry of the {@code answers} map of an Extractions
 * response - one answer per requested field, discriminated on its required
 * {@code type} property ({@code text}, {@code date}, {@code time},
 * {@code datetime}, {@code number}, {@code duration}, {@code list},
 * {@code estimate}, {@code forecast}).
 *
 * <p>Common to every type: {@link #value()} (the extracted value;
 * {@code null} when the state does not determine it - {@link #guess()} still
 * carries the most likely value then), {@link #pNull()} (the probability
 * that the state contains nothing for this field), {@link #confidence()},
 * {@link #source()} and {@link #spellConfidence()} (the confidence in the
 * spelling of text read from an image).
 *
 * <p>Per-type extras: {@code list} answers add {@link #confidences()} (one
 * per item of {@link #value()}, same order) and per-item guesses via
 * {@link #guessList()}; {@code estimate} answers add {@link #mean()},
 * {@link #median()}, {@link #p10()}, {@link #p90()}, {@link #pAbove()},
 * {@link #pBelow()} and the 99-row {@link #quantiles()} table;
 * {@code forecast} answers add the per-step series via {@link #forecast()}
 * and its flat array forms ({@link #meanSeries()} and friends).
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterExtractionAnswer {

    private final String name;
    private final JSONObject json;

    OpenRouterExtractionAnswer(String name, JSONObject json) {
        this.name = name;
        this.json = json;
    }

    /**
     * @return the field name this answer answers (the caller-chosen key of
     *         the request's {@code fields} map)
     */
    public String name() {
        return name;
    }

    /**
     * @return the raw answer object as received
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code type} - the answer discriminator.
     *
     * @return the type ({@code text}, {@code date}, {@code time},
     *         {@code datetime}, {@code number}, {@code duration},
     *         {@code list}, {@code estimate}, {@code forecast}), or
     *         {@code null} when absent
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code value} - the extracted value, {@code null} when the
     * state does not determine it. JSON-tolerant by type: {@code text},
     * {@code date}, {@code time} and {@code datetime} carry a string,
     * {@code number} and {@code duration} a number, {@code estimate} a
     * number or string (money values travel as strings), {@code list} and
     * {@code forecast} an array. Use the typed helpers
     * {@link #valueString()}, {@link #valueNumber()} and {@link #valueList()}.
     *
     * @return the value as {@link String}, {@link Number} or
     *         {@code List<Object>}, or {@code null} when absent, JSON null
     *         or neither
     */
    public Object value() {
        return raw("value");
    }

    /**
     * @return the {@code value} as a string (text-like answers), or
     *         {@code null} when absent, null or not a string
     */
    public String valueString() {
        Object value = value();
        return value instanceof String text ? text : null;
    }

    /**
     * @return the {@code value} as a number (numeric answers), or
     *         {@code null} when absent, null or not a number
     */
    public Number valueNumber() {
        Object value = value();
        return value instanceof Number number ? number : null;
    }

    /**
     * @return the {@code value} as the item list ({@code list} answers) or
     *         the per-step series ({@code forecast} answers), or
     *         {@code null} when absent, null or not an array
     */
    @SuppressWarnings("unchecked")
    public List<Object> valueList() {
        Object value = value();
        return value instanceof List<?> list ? (List<Object>) list : null;
    }

    /**
     * JSON path: {@code guess} - the most likely value even when
     * {@code value} is null because {@code p_null} crossed the threshold.
     * Same per-type shapes as {@link #value()} (per item for {@code list}
     * answers, per step for {@code forecast} answers).
     *
     * @return the guess as {@link String}, {@link Number} or
     *         {@code List<Object>}, or {@code null} when absent or neither
     */
    public Object guess() {
        return raw("guess");
    }

    /**
     * @return the {@code guess} as a string, or {@code null} when absent or
     *         not a string
     */
    public String guessString() {
        Object value = guess();
        return value instanceof String text ? text : null;
    }

    /**
     * @return the {@code guess} as a number, or {@code null} when absent or
     *         not a number
     */
    public Number guessNumber() {
        Object value = guess();
        return value instanceof Number number ? number : null;
    }

    /**
     * @return the {@code guess} as the per-item ({@code list}) or per-step
     *         ({@code forecast}) list, or {@code null} when absent or not
     *         an array
     */
    @SuppressWarnings("unchecked")
    public List<Object> guessList() {
        Object value = guess();
        return value instanceof List<?> list ? (List<Object>) list : null;
    }

    /**
     * JSON path: {@code p_null} - the probability that the state contains
     * nothing for this field. {@code value} is null when this crossed the
     * field's {@code null_threshold} (default 0.5).
     *
     * @return the probability, or {@code null} when absent or not a number
     */
    public Double pNull() {
        return doubleOf("p_null");
    }

    /**
     * JSON path: {@code confidence} - the confidence in the answer (0..1).
     *
     * @return the confidence, or {@code null} when absent or not a number
     */
    public Double confidence() {
        return doubleOf("confidence");
    }

    /**
     * JSON path: {@code source} - {@code state}, {@code image} or
     * {@code document}; set to {@code image} or {@code document} when the
     * answer was read from an image or a PDF rather than the state text.
     * Unknown spellings pass through verbatim.
     *
     * @return the source, or {@code null} when absent or not a string
     */
    public String source() {
        Object value = json.opt("source");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code spell_confidence} - confidence in the spelling of
     * text read from an image.
     *
     * @return the confidence, or {@code null} when absent or not a number
     */
    public Double spellConfidence() {
        return doubleOf("spell_confidence");
    }

    /**
     * JSON path: {@code span} - the {@code [start, end)} offsets of the
     * answer in the state text (scalar answers). The offsets index Unicode
     * code points, not UTF-16 code units or UTF-8 bytes, so an emoji or an
     * accented letter counts once.
     *
     * @return the two offsets, or {@code null} when absent or malformed
     */
    public List<Long> span() {
        try {
            JSONArray arr = json.optJSONArray("span");
            if (arr == null || arr.length() != 2) {
                return null;
            }
            Object start = arr.opt(0);
            Object end = arr.opt(1);
            if (!(start instanceof Number) || !(end instanceof Number)) {
                return null;
            }
            return List.of(((Number) start).longValue(), ((Number) end).longValue());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code spans} - one {@code [start, end)} pair per item
     * ({@code list} answers), in the item order. The offsets index Unicode
     * code points, not UTF-16 code units or UTF-8 bytes, so an emoji or an
     * accented letter counts once. Malformed pairs are dropped while good
     * siblings survive.
     *
     * @return the offset pairs, empty when absent or not an array
     */
    public List<List<Long>> spans() {
        List<List<Long>> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("spans");
            if (arr == null) {
                return List.of();
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONArray pair = arr.optJSONArray(i);
                if (pair == null || pair.length() != 2) {
                    continue;
                }
                Object start = pair.opt(0);
                Object end = pair.opt(1);
                if (!(start instanceof Number) || !(end instanceof Number)) {
                    continue;
                }
                result.add(List.of(((Number) start).longValue(), ((Number) end).longValue()));
            }
        } catch (Exception e) {
            // swallow: keep whatever was parsed before the failure
        }
        return List.copyOf(result);
    }

    /**
     * JSON path: {@code confidences} - one confidence per item of
     * {@code value}, in the same order ({@code list} answers). Malformed
     * entries surface as {@code null} so the one-per-item alignment with
     * {@link #valueList()} survives.
     *
     * @return the per-item confidences, empty when absent or not an array
     */
    public List<Double> confidences() {
        List<Double> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("confidences");
            if (arr == null) {
                return List.of();
            }
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.opt(i);
                result.add(value instanceof Number number ? number.doubleValue() : null);
            }
        } catch (Exception e) {
            // swallow: keep whatever was parsed before the failure
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * JSON path: {@code mean} - the mean of the estimated distribution
     * ({@code estimate} answers; number or string on the wire - money
     * values travel as strings). {@code forecast} answers carry the
     * per-step series under this key instead; read it via
     * {@link #meanSeries()} or {@link #forecast()}.
     *
     * @return the mean as {@link Number} or {@link String}, or {@code null}
     *         when absent or neither
     */
    public Object mean() {
        return scalar("mean");
    }

    /**
     * JSON path: {@code median} - the median of the estimated distribution
     * ({@code estimate} answers; number or string on the wire).
     *
     * @return the median as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object median() {
        return scalar("median");
    }

    /**
     * JSON path: {@code p10} - the 10th percentile of the estimated
     * distribution ({@code estimate} answers; number or string on the wire).
     *
     * @return the percentile as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object p10() {
        return scalar("p10");
    }

    /**
     * JSON path: {@code p90} - the 90th percentile of the estimated
     * distribution ({@code estimate} answers; number or string on the wire).
     *
     * @return the percentile as {@link Number} or {@link String}, or
     *         {@code null} when absent or neither
     */
    public Object p90() {
        return scalar("p90");
    }

    /**
     * JSON path: {@code p_above} - the probability mass above {@code max}
     * ({@code estimate} answers).
     *
     * @return the mass, or {@code null} when absent or not a number
     */
    public Double pAbove() {
        return doubleOf("p_above");
    }

    /**
     * JSON path: {@code p_below} - the probability mass below {@code min}
     * ({@code estimate} answers).
     *
     * @return the mass, or {@code null} when absent or not a number
     */
    public Double pBelow() {
        return doubleOf("p_below");
    }

    /**
     * JSON path: {@code quantiles} - the 99-percentile table of the
     * estimated distribution ({@code estimate} answers, present when the
     * provider is asked for quantiles). Raw rows as number or string.
     *
     * @return the quantile rows, empty when absent or not an array
     */
    public List<Object> quantiles() {
        return objectList("quantiles");
    }

    /**
     * JSON path: {@code mean} (array form) - the per-step mean series
     * ({@code forecast} answers; the same values as the per-step
     * {@link OpenRouterExtractionForecastStep#mean()}).
     *
     * @return the series, empty when absent or not an array
     */
    public List<Object> meanSeries() {
        return objectList("mean");
    }

    /**
     * JSON path: {@code median} (array form) - the per-step median series
     * ({@code forecast} answers).
     *
     * @return the series, empty when absent or not an array
     */
    public List<Object> medianSeries() {
        return objectList("median");
    }

    /**
     * JSON path: {@code p10} (array form) - the per-step 10th-percentile
     * series ({@code forecast} answers).
     *
     * @return the series, empty when absent or not an array
     */
    public List<Object> p10Series() {
        return objectList("p10");
    }

    /**
     * JSON path: {@code p90} (array form) - the per-step 90th-percentile
     * series ({@code forecast} answers).
     *
     * @return the series, empty when absent or not an array
     */
    public List<Object> p90Series() {
        return objectList("p90");
    }

    /**
     * JSON path: {@code steps} - the per-step forecast series
     * ({@code forecast} answers). Non-object entries are dropped while good
     * siblings survive.
     *
     * @return the typed steps, empty when absent or not an array
     */
    public List<OpenRouterExtractionForecastStep> forecast() {
        List<OpenRouterExtractionForecastStep> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("steps");
            if (arr == null) {
                return List.of();
            }
            for (int i = 0; i < arr.length(); i++) {
                JSONObject step = arr.optJSONObject(i);
                if (step != null) {
                    result.add(new OpenRouterExtractionForecastStep(step));
                }
            }
        } catch (Exception e) {
            // swallow: keep whatever was parsed before the failure
        }
        return List.copyOf(result);
    }

    private Object raw(String key) {
        try {
            return normalize(json.opt(key));
        } catch (Exception e) {
            return null;
        }
    }

    private Object scalar(String key) {
        try {
            Object value = json.opt(key);
            return value instanceof Number || value instanceof String ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Double doubleOf(String key) {
        try {
            Object value = json.opt(key);
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Object> objectList(String key) {
        List<Object> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray(key);
            if (arr == null) {
                return List.of();
            }
            for (int i = 0; i < arr.length(); i++) {
                result.add(normalize(arr.opt(i)));
            }
        } catch (Exception e) {
            // swallow: keep whatever was parsed before the failure
        }
        return Collections.unmodifiableList(result);
    }

    private static Object normalize(Object value) {
        if (value == null || JSONObject.NULL.equals(value)) {
            return null;
        }
        if (value instanceof JSONArray arr) {
            List<Object> result = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                result.add(normalize(arr.opt(i)));
            }
            return Collections.unmodifiableList(result);
        }
        // only the documented shapes survive: string, number, array
        return value instanceof String || value instanceof Number ? value : null;
    }
}
