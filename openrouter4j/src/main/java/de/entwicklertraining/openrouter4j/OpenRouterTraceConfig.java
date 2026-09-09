package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Trace configuration sent as the {@code trace} request field of a chat
 * completion - metadata for observability and tracing that OpenRouter
 * forwards to configured broadcast destinations (Langfuse, Datadog, Weave,
 * ...). The keys the OpenRouter schema names are available as typed methods;
 * any additional key is passed through verbatim as custom metadata, because
 * the schema allows arbitrary additional properties.
 * <p>
 * JSON field: {@code trace}. Default: unset (the key is not sent).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/broadcast">Broadcast</a>
 */
public final class OpenRouterTraceConfig {

    private static final java.util.Set<String> KNOWN_KEYS = java.util.Set.of(
            "trace_id", "trace_name", "span_name", "generation_name", "parent_span_id");

    private final Map<String, Object> options;

    private OpenRouterTraceConfig(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * Creates a new, empty builder for {@code trace}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the configured {@code trace.trace_id} value, or {@code null}
     * when unset (the key is not sent).
     */
    public String traceId() {
        return stringValue("trace_id");
    }

    /**
     * Returns the configured {@code trace.trace_name} value, or {@code null}
     * when unset (the key is not sent).
     */
    public String traceName() {
        return stringValue("trace_name");
    }

    /**
     * Returns the configured {@code trace.span_name} value, or {@code null}
     * when unset (the key is not sent).
     */
    public String spanName() {
        return stringValue("span_name");
    }

    /**
     * Returns the configured {@code trace.generation_name} value, or
     * {@code null} when unset (the key is not sent).
     */
    public String generationName() {
        return stringValue("generation_name");
    }

    /**
     * Returns the configured {@code trace.parent_span_id} value, or
     * {@code null} when unset (the key is not sent).
     */
    public String parentSpanId() {
        return stringValue("parent_span_id");
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when unset.
     */
    public Object option(String key) {
        return options.get(key);
    }

    /**
     * Returns an unmodifiable copy of all configured options, in insertion order.
     */
    public Map<String, Object> options() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(options));
    }

    /**
     * Returns the JSON object emitted as the {@code trace} request field.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : options.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    private String stringValue(String key) {
        Object value = options.get(key);
        return value instanceof String s ? s : null;
    }

    /** Builder for {@link OpenRouterTraceConfig}. */
    public static final class Builder {

        private final Map<String, Object> options = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code trace_id}: the identifier of the trace this generation
         * belongs to. Use it to group all requests of one multi-step workflow
         * under a single trace in the broadcast destination.
         */
        public Builder traceId(String traceId) {
            options.put("trace_id", traceId);
            return this;
        }

        /**
         * Sets {@code trace_name}: the human-readable name of the root trace,
         * shown as the trace's display name in the broadcast destination.
         */
        public Builder traceName(String traceName) {
            options.put("trace_name", traceName);
            return this;
        }

        /**
         * Sets {@code span_name}: the name of the span this generation opens
         * inside its trace.
         */
        public Builder spanName(String spanName) {
            options.put("span_name", spanName);
            return this;
        }

        /**
         * Sets {@code generation_name}: the display name of this single
         * generation within the trace.
         */
        public Builder generationName(String generationName) {
            options.put("generation_name", generationName);
            return this;
        }

        /**
         * Sets {@code parent_span_id}: the identifier of the parent span, used
         * to link this generation as a child of an existing span (e.g. an
         * OpenTelemetry span id from your own tracing system).
         */
        public Builder parentSpanId(String parentSpanId) {
            options.put("parent_span_id", parentSpanId);
            return this;
        }

        /**
         * Sets an arbitrary {@code trace} key verbatim. Use this for custom
         * metadata keys without a typed convenience method - the schema allows
         * additional properties, and OpenRouter forwards them to configured
         * broadcast destinations.
         * <p>
         * Trap: the five keys the schema names explicitly
         * ({@code trace_id}, {@code trace_name}, {@code span_name},
         * {@code generation_name}, {@code parent_span_id}) must be set through
         * their typed methods - this method rejects them so the wire payload
         * cannot diverge.
         *
         * @param key the custom metadata key
         * @param value the metadata value (String, Number, Boolean or null)
         */
        public Builder option(String key, Object value) {
            if (KNOWN_KEYS.contains(key)) {
                throw new IllegalArgumentException(
                        "Trace key '" + key + "' must be set through its typed method");
            }
            options.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterTraceConfig} value type.
         */
        public OpenRouterTraceConfig build() {
            return new OpenRouterTraceConfig(new LinkedHashMap<>(options));
        }
    }
}
