package de.entwicklertraining.openrouter4j.extractions;

import org.json.JSONObject;

/**
 * A typed field spec of an Extractions request - one entry of the
 * {@code fields} map of {@link OpenRouterExtractionsRequest}
 * (POST /api/alpha/extractions), keyed by the caller-chosen field name that
 * the response's {@code answers} map echoes. Discriminated on the required
 * {@code type} property:
 * <ul>
 *   <li>{@code text} / {@code date} / {@code time} / {@code datetime} -
 *   optional {@code instructions} and {@code null_threshold}
 *   ({@link #text()}, {@link #date()}, {@link #time()}, {@link #datetime()}).</li>
 *   <li>{@code number} / {@code duration} - additionally optional
 *   {@code decimals}, {@code integer} and {@code unit}
 *   ({@link #number()}, {@link #duration()}).</li>
 *   <li>{@code list} - additionally required {@code items} plus optional
 *   {@code decimals} ({@link #list()}).</li>
 *   <li>{@code estimate} - additionally required {@code min} and {@code max},
 *   optional {@code scale}, {@code step} and {@code unit} ({@link #estimate()}).</li>
 *   <li>{@code forecast} - additionally required {@code min}, {@code max},
 *   {@code horizon} and {@code interval}, optional {@code scale}, {@code step}
 *   and {@code unit} ({@link #forecast()}).</li>
 * </ul>
 *
 * <p>Create instances through the typed factories and their per-type
 * builders, or through the verbatim escape hatch {@link #raw(JSONObject)}
 * for field types OpenRouter adds after this library was released. Every
 * spec schema is {@code additionalProperties: false}, so a typed spec emits
 * only the documented keys of its type.
 *
 * <p>Traps: which field types a model answers is model-specific;
 * <b>unsupported types are rejected with 400</b>. {@code null_threshold} is
 * the probability of null above which the answer's {@code value} is null
 * (default 0.5). The {@code items}, {@code scale} and {@code interval}
 * spellings are passed through verbatim - the documented values are
 * {@code text}/{@code number}/{@code date}, {@code linear}/{@code log} and
 * {@code step}/{@code minute}/{@code hour}/{@code day}/{@code week}/
 * {@code month}/{@code quarter}/{@code year}, but unknown spellings are
 * emitted as written and rejected by the API, not by this library. The
 * {@code step} of {@code estimate}/{@code forecast} is closed on the wire
 * (a positive number, or the strings {@code month}/{@code year}) and is
 * therefore validated loudly here.
 *
 * <p>Alpha: this schema belongs to the {@code /api/alpha} namespace and may
 * change or disappear without a major version of the API.
 */
public final class OpenRouterExtractionField {

    private final JSONObject json;

    private OpenRouterExtractionField(JSONObject json) {
        this.json = json;
    }

    /**
     * Creates a {@code text} field spec builder
     * ({@code {"type":"text", ...}}) - extract free text from the state.
     *
     * @return the per-type builder
     */
    public static TextBuilder text() {
        return new TextBuilder();
    }

    /**
     * Creates a {@code date} field spec builder
     * ({@code {"type":"date", ...}}) - extract a date. Dates written without
     * a year are anchored on the request's {@code reference_date}.
     *
     * @return the per-type builder
     */
    public static DateBuilder date() {
        return new DateBuilder();
    }

    /**
     * Creates a {@code time} field spec builder
     * ({@code {"type":"time", ...}}) - extract a time of day.
     *
     * @return the per-type builder
     */
    public static TimeBuilder time() {
        return new TimeBuilder();
    }

    /**
     * Creates a {@code datetime} field spec builder
     * ({@code {"type":"datetime", ...}}) - extract a date-time.
     *
     * @return the per-type builder
     */
    public static DatetimeBuilder datetime() {
        return new DatetimeBuilder();
    }

    /**
     * Creates a {@code number} field spec builder
     * ({@code {"type":"number", ...}}) - compute or extract a number.
     *
     * @return the per-type builder
     */
    public static NumberBuilder number() {
        return new NumberBuilder();
    }

    /**
     * Creates a {@code duration} field spec builder
     * ({@code {"type":"duration", ...}}) - compute a duration. The
     * {@code unit} description names seconds, minutes, hours, days, weeks,
     * months or years.
     *
     * @return the per-type builder
     */
    public static DurationBuilder duration() {
        return new DurationBuilder();
    }

    /**
     * Creates a {@code list} field spec builder
     * ({@code {"type":"list", ...}}) - extract a list of items of one
     * element type.
     *
     * @return the per-type builder
     */
    public static ListBuilder list() {
        return new ListBuilder();
    }

    /**
     * Creates an {@code estimate} field spec builder
     * ({@code {"type":"estimate", ...}}) - estimate a value inside
     * {@code min}..{@code max} as a distribution.
     *
     * @return the per-type builder
     */
    public static EstimateBuilder estimate() {
        return new EstimateBuilder();
    }

    /**
     * Creates a {@code forecast} field spec builder
     * ({@code {"type":"forecast", ...}}) - forecast a series of
     * {@code horizon} steps at the given {@code interval}.
     *
     * @return the per-type builder
     */
    public static ForecastBuilder forecast() {
        return new ForecastBuilder();
    }

    /**
     * Creates a field spec verbatim - the escape hatch for field types
     * OpenRouter adds after this library was released. The object is
     * emitted as written (including any unknown keys), so it can also carry
     * option spellings this library does not know yet. The object must
     * carry a {@code type} field.
     *
     * @param json the verbatim field spec object
     * @return the field spec
     */
    public static OpenRouterExtractionField raw(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("field spec must not be null");
        }
        if (json.optString("type", null) == null) {
            throw new IllegalArgumentException("a raw field spec must carry a \"type\" field");
        }
        return new OpenRouterExtractionField(new JSONObject(json.toString()));
    }

    /**
     * @return the discriminator value ({@code text}, {@code date},
     *         {@code time}, {@code datetime}, {@code number}, {@code duration},
     *         {@code list}, {@code estimate}, {@code forecast} or the raw type
     *         of an escape-hatch spec), or {@code null} when absent
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code instructions} - what the field should contain.
     *
     * @return the instructions, or {@code null} when unset or not a string
     */
    public String instructions() {
        Object value = json.opt("instructions");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code null_threshold} - probability of null above which
     * the answer's {@code value} is null (default 0.5).
     *
     * @return the threshold within 0..1, or {@code null} when unset
     */
    public Double nullThreshold() {
        Object value = json.opt("null_threshold");
        return value instanceof Number number ? number.doubleValue() : null;
    }

    /**
     * JSON path: {@code decimals} - decimal places kept in the answer's
     * {@code value} ({@code number}, {@code duration} and {@code list}).
     *
     * @return the decimal places within 0..6, or {@code null} when unset
     */
    public Integer decimals() {
        Object value = json.opt("decimals");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code integer} - whether the answer must be an integer
     * ({@code number} and {@code duration}).
     *
     * @return the flag, or {@code null} when unset or not a boolean
     */
    public Boolean integer() {
        Object value = json.opt("integer");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code unit} - the unit the value is expressed in
     * ({@code number}, {@code duration}, {@code estimate}, {@code forecast}).
     * Free-form; the duration unit description names seconds, minutes,
     * hours, days, weeks, months or years.
     *
     * @return the unit, or {@code null} when unset or not a string
     */
    public String unit() {
        Object value = json.opt("unit");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code items} - the element type of a {@code list} field
     * ({@code text}, {@code number} or {@code date}; unknown spellings pass
     * through verbatim).
     *
     * @return the element type, or {@code null} when unset or not a string
     */
    public String items() {
        Object value = json.opt("items");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code min} - the lower bound of {@code estimate} and
     * {@code forecast} (number or string on the wire; money values travel
     * as strings).
     *
     * @return the bound as {@link Number} or {@link String}, or {@code null}
     *         when absent or neither
     */
    public Object min() {
        return scalar("min");
    }

    /**
     * JSON path: {@code max} - the upper bound of {@code estimate} and
     * {@code forecast} (number or string on the wire; money values travel
     * as strings).
     *
     * @return the bound as {@link Number} or {@link String}, or {@code null}
     *         when absent or neither
     */
    public Object max() {
        return scalar("max");
    }

    /**
     * JSON path: {@code scale} - the scale of {@code estimate} and
     * {@code forecast} ({@code linear} or {@code log}; unknown spellings
     * pass through verbatim).
     *
     * @return the scale, or {@code null} when unset or not a string
     */
    public String scale() {
        Object value = json.opt("scale");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code step} - the step of {@code estimate} and
     * {@code forecast}: a positive number, or the strings {@code month} /
     * {@code year}.
     *
     * @return the step as {@link Number} or {@link String}, or {@code null}
     *         when absent or neither
     */
    public Object step() {
        return scalar("step");
    }

    /**
     * JSON path: {@code horizon} - the number of forecast steps (1..64).
     *
     * @return the horizon, or {@code null} when unset
     */
    public Integer horizon() {
        Object value = json.opt("horizon");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code interval} - the interval between forecast steps
     * ({@code step}, {@code minute}, {@code hour}, {@code day}, {@code week},
     * {@code month}, {@code quarter} or {@code year}; unknown spellings pass
     * through verbatim).
     *
     * @return the interval, or {@code null} when unset or not a string
     */
    public String interval() {
        Object value = json.opt("interval");
        return value instanceof String text ? text : null;
    }

    /**
     * @return the JSON object emitted as the {@code fields[name]} value
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    private Object scalar(String key) {
        Object value = json.opt(key);
        return value instanceof Number || value instanceof String ? value : null;
    }

    private abstract static class CommonBuilder<B extends CommonBuilder<B>> {

        private String instructions;
        private Double nullThreshold;

        @SuppressWarnings("unchecked")
        B self() {
            return (B) this;
        }

        /**
         * Sets the JSON field {@code instructions} - what the field should
         * contain. Omitted when unset ({@code null}).
         *
         * @param instructions the field instructions
         * @return this builder
         */
        public B instructions(String instructions) {
            this.instructions = instructions;
            return self();
        }

        /**
         * Sets the JSON field {@code null_threshold} - the probability of
         * null above which the answer's {@code value} is null (default
         * 0.5). Validated loudly: must be within 0..1 (boundaries pass).
         * Omitted when unset ({@code null}).
         *
         * @param nullThreshold the threshold within 0..1
         * @return this builder
         */
        public B nullThreshold(Double nullThreshold) {
            if (nullThreshold != null && (nullThreshold < 0 || nullThreshold > 1)) {
                throw new IllegalArgumentException(
                        "null_threshold must be within 0..1 (default 0.5)");
            }
            this.nullThreshold = nullThreshold;
            return self();
        }

        /**
         * Builds the immutable field spec. Only the documented keys of the
         * spec's {@code type} are emitted (the spec schemas are
         * {@code additionalProperties: false}), unset options stay out of
         * the JSON.
         *
         * @return the field spec
         */
        public final OpenRouterExtractionField build() {
            JSONObject json = new JSONObject();
            json.put("type", type());
            if (instructions != null) {
                json.put("instructions", instructions);
            }
            if (nullThreshold != null) {
                json.put("null_threshold", nullThreshold);
            }
            emitOptions(json);
            return new OpenRouterExtractionField(json);
        }

        abstract String type();

        abstract void emitOptions(JSONObject json);
    }

    /**
     * Builder for the {@code text}, {@code date}, {@code time} and
     * {@code datetime} field specs - the four specs that carry no options
     * beyond {@code instructions} and {@code null_threshold}.
     */
    public abstract static class SimpleBuilder<B extends SimpleBuilder<B>> extends CommonBuilder<B> {
    }

    /** Builder for the {@code text} field spec. */
    public static final class TextBuilder extends SimpleBuilder<TextBuilder> {

        private TextBuilder() {
        }

        @Override
        String type() {
            return "text";
        }

        @Override
        void emitOptions(JSONObject json) {
            // no options beyond instructions / null_threshold
        }
    }

    /** Builder for the {@code date} field spec. */
    public static final class DateBuilder extends SimpleBuilder<DateBuilder> {

        private DateBuilder() {
        }

        @Override
        String type() {
            return "date";
        }

        @Override
        void emitOptions(JSONObject json) {
            // no options beyond instructions / null_threshold
        }
    }

    /** Builder for the {@code time} field spec. */
    public static final class TimeBuilder extends SimpleBuilder<TimeBuilder> {

        private TimeBuilder() {
        }

        @Override
        String type() {
            return "time";
        }

        @Override
        void emitOptions(JSONObject json) {
            // no options beyond instructions / null_threshold
        }
    }

    /** Builder for the {@code datetime} field spec. */
    public static final class DatetimeBuilder extends SimpleBuilder<DatetimeBuilder> {

        private DatetimeBuilder() {
        }

        @Override
        String type() {
            return "datetime";
        }

        @Override
        void emitOptions(JSONObject json) {
            // no options beyond instructions / null_threshold
        }
    }

    private abstract static class NumericBuilder<B extends NumericBuilder<B>> extends CommonBuilder<B> {

        private Integer decimals;
        private Boolean integer;
        private String unit;

        /**
         * Sets the JSON field {@code decimals} - decimal places kept in the
         * answer's {@code value}. Validated loudly: must be within 0..6
         * (boundaries pass). Omitted when unset ({@code null}).
         *
         * @param decimals the decimal places within 0..6
         * @return this builder
         */
        public B decimals(Integer decimals) {
            if (decimals != null && (decimals < 0 || decimals > 6)) {
                throw new IllegalArgumentException("decimals must be within 0..6");
            }
            this.decimals = decimals;
            return self();
        }

        /**
         * Sets the JSON field {@code integer} - whether the answer must be
         * an integer. Omitted when unset ({@code null}).
         *
         * @param integer the flag
         * @return this builder
         */
        public B integer(Boolean integer) {
            this.integer = integer;
            return self();
        }

        /**
         * Sets the JSON field {@code unit} - the unit the value is expressed
         * in. Free-form; the duration unit description names seconds,
         * minutes, hours, days, weeks, months or years. Omitted when unset
         * ({@code null}).
         *
         * @param unit the unit description
         * @return this builder
         */
        public B unit(String unit) {
            this.unit = unit;
            return self();
        }

        @Override
        void emitOptions(JSONObject json) {
            if (decimals != null) {
                json.put("decimals", decimals);
            }
            if (integer != null) {
                json.put("integer", integer);
            }
            if (unit != null) {
                json.put("unit", unit);
            }
        }
    }

    /** Builder for the {@code number} field spec. */
    public static final class NumberBuilder extends NumericBuilder<NumberBuilder> {

        private NumberBuilder() {
        }

        @Override
        String type() {
            return "number";
        }
    }

    /** Builder for the {@code duration} field spec. */
    public static final class DurationBuilder extends NumericBuilder<DurationBuilder> {

        private DurationBuilder() {
        }

        @Override
        String type() {
            return "duration";
        }
    }

    /**
     * Builder for the {@code list} field spec.
     */
    public static final class ListBuilder extends CommonBuilder<ListBuilder> {

        private String items;
        private Integer decimals;

        private ListBuilder() {
        }

        /**
         * Sets the JSON field {@code items} - the element type of the list
         * ({@code text}, {@code number} or {@code date}). Unknown spellings
         * pass through verbatim; the API rejects what it does not know.
         * Omitted when unset ({@code null}).
         *
         * @param items the element type
         * @return this builder
         */
        public ListBuilder items(String items) {
            this.items = items;
            return self();
        }

        /**
         * Sets the JSON field {@code decimals} - decimal places kept in the
         * answer's {@code value} items. Validated loudly: must be within
         * 0..6 (boundaries pass). Omitted when unset ({@code null}).
         *
         * @param decimals the decimal places within 0..6
         * @return this builder
         */
        public ListBuilder decimals(Integer decimals) {
            if (decimals != null && (decimals < 0 || decimals > 6)) {
                throw new IllegalArgumentException("decimals must be within 0..6");
            }
            this.decimals = decimals;
            return self();
        }

        @Override
        String type() {
            return "list";
        }

        @Override
        void emitOptions(JSONObject json) {
            if (items == null) {
                throw new IllegalStateException(
                        "list requires items - use items(\"text\"|\"number\"|\"date\")");
            }
            json.put("items", items);
            if (decimals != null) {
                json.put("decimals", decimals);
            }
        }
    }

    private abstract static class RangeBuilder<B extends RangeBuilder<B>> extends CommonBuilder<B> {

        private Object min;
        private Object max;
        private String scale;
        private Object step;
        private String unit;

        /**
         * Sets the JSON field {@code min} - the lower bound (number form).
         * Required for {@code estimate} and {@code forecast}, checked in
         * {@link #build()}.
         *
         * @param min the lower bound
         * @return this builder
         */
        public B min(Number min) {
            this.min = min;
            return self();
        }

        /**
         * Sets the JSON field {@code min} - the lower bound (string form,
         * for money values that travel as strings). Required for
         * {@code estimate} and {@code forecast}, checked in {@link #build()}.
         *
         * @param min the lower bound
         * @return this builder
         */
        public B min(String min) {
            this.min = min;
            return self();
        }

        /**
         * Sets the JSON field {@code max} - the upper bound (number form).
         * Required for {@code estimate} and {@code forecast}, checked in
         * {@link #build()}.
         *
         * @param max the upper bound
         * @return this builder
         */
        public B max(Number max) {
            this.max = max;
            return self();
        }

        /**
         * Sets the JSON field {@code max} - the upper bound (string form,
         * for money values that travel as strings). Required for
         * {@code estimate} and {@code forecast}, checked in {@link #build()}.
         *
         * @param max the upper bound
         * @return this builder
         */
        public B max(String max) {
            this.max = max;
            return self();
        }

        /**
         * Sets the JSON field {@code scale} - {@code linear} or {@code log}.
         * Unknown spellings pass through verbatim; the API rejects what it
         * does not know. Omitted when unset ({@code null}).
         *
         * @param scale the scale spelling
         * @return this builder
         */
        public B scale(String scale) {
            this.scale = scale;
            return self();
        }

        /**
         * Sets the JSON field {@code step} - the step size as a positive
         * number. Validated loudly: must be greater than 0. Omitted when
         * unset ({@code null}).
         *
         * @param step the positive step size
         * @return this builder
         */
        public B step(Number step) {
            if (step != null && step.doubleValue() <= 0) {
                throw new IllegalArgumentException("step must be a positive number");
            }
            this.step = step;
            return self();
        }

        /**
         * Sets the JSON field {@code step} - the step size as one of the
         * closed string spellings {@code month} or {@code year}. Validated
         * loudly (the wire form is a closed enum, unlike {@code scale} and
         * {@code interval}). Omitted when unset ({@code null}).
         *
         * @param step {@code month} or {@code year}
         * @return this builder
         */
        public B step(String step) {
            if (step != null && !step.equals("month") && !step.equals("year")) {
                throw new IllegalArgumentException(
                        "step must be a positive number or one of \"month\" / \"year\"");
            }
            this.step = step;
            return self();
        }

        /**
         * Sets the JSON field {@code unit} - the unit the value is expressed
         * in. Free-form. Omitted when unset ({@code null}).
         *
         * @param unit the unit description
         * @return this builder
         */
        public B unit(String unit) {
            this.unit = unit;
            return self();
        }

        @Override
        void emitOptions(JSONObject json) {
            if (min == null || max == null) {
                throw new IllegalStateException(type() + " requires min and max");
            }
            json.put("min", min);
            json.put("max", max);
            if (scale != null) {
                json.put("scale", scale);
            }
            if (step != null) {
                json.put("step", step);
            }
            if (unit != null) {
                json.put("unit", unit);
            }
        }
    }

    /**
     * Builder for the {@code estimate} field spec. {@code min} and
     * {@code max} are required and checked in {@link #build()}.
     */
    public static final class EstimateBuilder extends RangeBuilder<EstimateBuilder> {

        private EstimateBuilder() {
        }

        @Override
        String type() {
            return "estimate";
        }
    }

    /**
     * Builder for the {@code forecast} field spec. {@code min}, {@code max},
     * {@code horizon} and {@code interval} are required and checked in
     * {@link #build()}.
     */
    public static final class ForecastBuilder extends RangeBuilder<ForecastBuilder> {

        private Integer horizon;
        private String interval;

        private ForecastBuilder() {
        }

        /**
         * Sets the JSON field {@code horizon} - the number of forecast
         * steps. Validated loudly: must be within 1..64 (boundaries pass).
         * Required for {@code forecast}, checked in {@link #build()}.
         * Omitted when unset ({@code null}).
         *
         * @param horizon the number of steps within 1..64
         * @return this builder
         */
        public ForecastBuilder horizon(Integer horizon) {
            if (horizon != null && (horizon < 1 || horizon > 64)) {
                throw new IllegalArgumentException("horizon must be within 1..64");
            }
            this.horizon = horizon;
            return self();
        }

        /**
         * Sets the JSON field {@code interval} - the interval between
         * forecast steps ({@code step}, {@code minute}, {@code hour},
         * {@code day}, {@code week}, {@code month}, {@code quarter} or
         * {@code year}). Unknown spellings pass through verbatim; the API
         * rejects what it does not know. Required for {@code forecast},
         * checked in {@link #build()}. Omitted when unset ({@code null}).
         *
         * @param interval the interval spelling
         * @return this builder
         */
        public ForecastBuilder interval(String interval) {
            this.interval = interval;
            return self();
        }

        @Override
        String type() {
            return "forecast";
        }

        @Override
        void emitOptions(JSONObject json) {
            super.emitOptions(json);
            if (horizon == null || interval == null) {
                throw new IllegalStateException("forecast requires horizon and interval");
            }
            json.put("horizon", horizon);
            json.put("interval", interval);
        }
    }
}
