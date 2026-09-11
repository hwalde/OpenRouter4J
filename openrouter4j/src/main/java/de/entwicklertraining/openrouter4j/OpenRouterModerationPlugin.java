package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code moderation} plugin: guards the
 * request with OpenRouter's moderation layer. The plugin carries no
 * configuration in the API schema - it is emitted as {@code {"id":
 * "moderation"}}.
 * <p>
 * JSON field: {@code plugins[].id = "moderation"}. Default: no plugin is sent.
 * <p>
 * Trap: whether the moderation plugin is actually honoured depends on the
 * endpoint routing the request; use the router metadata
 * ({@code metadataInResponse(true)}) pipeline stages to see whether a
 * guardrail stage ran.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterModerationPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "moderation";

    private final Map<String, Object> extraOptions;

    private OpenRouterModerationPlugin(Builder builder) {
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates the {@code moderation} plugin with no extra fields.
     */
    public OpenRouterModerationPlugin() {
        this(new Builder());
    }

    /**
     * Creates a new builder for the {@code moderation} plugin (the schema
     * defines no configuration keys, so the plugin is usually built
     * with the no-arg constructor).
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "moderation"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code moderation} plugin. The API schema defines no
     * configuration keys for this plugin; a verbatim {@link #option(String,
     * Object)} escape hatch covers keys OpenRouter may add later.
     */
    public static final class Builder {

        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
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
         * Builds the {@link OpenRouterModerationPlugin} value type.
         */
        public OpenRouterModerationPlugin build() {
            return new OpenRouterModerationPlugin(this);
        }
    }
}
