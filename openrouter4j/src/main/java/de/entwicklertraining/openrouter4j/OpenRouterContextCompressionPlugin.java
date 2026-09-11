package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code context-compression} plugin:
 * compresses the conversation context (e.g. the {@code middle-out} engine)
 * when it exceeds the endpoint's budget. Emitted into the {@code plugins}
 * request array as {@code {"id": "context-compression", ...}} with only the
 * explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "context-compression"}. Default: no plugin
 * is sent.
 * <p>
 * Trap: when the plugin runs, it appears as a {@code context_compression}
 * stage in the router metadata pipeline (opt-in via
 * {@code metadataInResponse(true)}); a no-op compression (input already fits)
 * is omitted from the pipeline.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterContextCompressionPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "context-compression";

    private final Boolean enabled;
    private final String engine;
    private final Map<String, Object> extraOptions;

    private OpenRouterContextCompressionPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.engine = builder.engine;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code context-compression} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "context-compression"} ({@code plugins[].id}). */
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

    /**
     * Returns the configured {@code engine} value. The schema documents
     * {@code "middle-out"} (the default when the key is unset).
     */
    public String engine() {
        return engine;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        if (engine != null) {
            json.put("engine", engine);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code context-compression} plugin. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private Boolean enabled;
        private String engine;
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
         * Sets {@code engine}: the compression engine. The schema documents
         * {@code "middle-out"} (also the default when the key is unset).
         */
        public Builder engine(String engine) {
            this.engine = engine;
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
         * Builds the {@link OpenRouterContextCompressionPlugin} value type.
         */
        public OpenRouterContextCompressionPlugin build() {
            return new OpenRouterContextCompressionPlugin(this);
        }
    }
}
