package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code response-healing} plugin:
 * repairs malformed JSON responses so structured outputs arrive valid. Emitted
 * into the {@code plugins} request array as {@code {"id": "response-healing",
 * ...}} with only the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "response-healing"}. Default: no plugin is
 * sent.
 * <p>
 * Trap: healing adds latency when it actually repairs a response; combine it
 * with structured outputs ({@code responseSchema(...)}) rather than sending it
 * on every request.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins/response-healing">Response healing plugin</a>
 */
public final class OpenRouterResponseHealingPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "response-healing";

    private final Boolean enabled;
    private final Map<String, Object> extraOptions;

    private OpenRouterResponseHealingPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates the {@code response-healing} plugin with no extra fields.
     */
    public OpenRouterResponseHealingPlugin() {
        this(new Builder());
    }

    /**
     * Creates a new builder for the {@code response-healing} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "response-healing"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code enabled} value, or {@code null} when unset
     * (the key is not sent and {@code true} applies).
     */
    public Boolean enabled() {
        return enabled;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code response-healing} plugin. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers keys OpenRouter may add later.
     */
    public static final class Builder {

        private Boolean enabled;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code enabled}: set to {@code false} to disable the plugin for
         * this request. Default: unset ({@code true} applies).
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"}. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterResponseHealingPlugin} value type.
         */
        public OpenRouterResponseHealingPlugin build() {
            return new OpenRouterResponseHealingPlugin(this);
        }
    }
}
