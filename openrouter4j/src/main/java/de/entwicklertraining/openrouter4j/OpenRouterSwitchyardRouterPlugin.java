package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenRouter {@code switchyard-router} plugin:
 * routes the request between eligible model tiers using the configured
 * algorithm. Emitted into the {@code plugins} request array as
 * {@code {"id": "switchyard-router", ...}} with only the explicitly configured
 * fields.
 * <p>
 * JSON field: {@code plugins[].id = "switchyard-router"}. Default: no plugin is
 * sent.
 * <p>
 * Algorithm semantics (per the OpenAPI schema):
 * <ul>
 *   <li>{@code "capability"} - calls a small judge model to rate how demanding
 *       the task is, then picks the efficient or capable candidate.</li>
 *   <li>{@code "stage"} - reads the tool-result history (errors, repeated
 *       failures, edits landing) and calls the judge only when those signals
 *       are undecided.</li>
 *   <li>{@code "auto"} - {@code "stage"} without the judge call.</li>
 *   <li>{@code "random"} - picks one candidate at random.</li>
 *   <li>{@code "composite"} - keeps the tier chosen on the last human turn and
 *       re-evaluates tool turns with the stage signals.</li>
 *   <li>{@code "passthrough"} - serves the eligible candidates in OpenRouter's
 *       ranked order with no routing decision and no judge call.</li>
 * </ul>
 * Omitted {@code algorithm} = platform default ({@code "capability"}).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterSwitchyardRouterPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "switchyard-router";

    private final String algorithm;
    private final Map<String, Object> extraOptions;

    private OpenRouterSwitchyardRouterPlugin(Builder builder) {
        this.algorithm = builder.algorithm;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code switchyard-router} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "switchyard-router"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code algorithm} value
     * ({@code "capability"}, {@code "stage"}, {@code "auto"}, {@code "random"},
     * {@code "composite"} or {@code "passthrough"}), or {@code null} when unset
     * (the platform default {@code "capability"} applies).
     */
    public String algorithm() {
        return algorithm;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (algorithm != null) {
            json.put("algorithm", algorithm);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code switchyard-router} plugin. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private String algorithm;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code algorithm}: the routing algorithm. Documented values:
         * {@code "capability"}, {@code "stage"}, {@code "auto"}, {@code "random"},
         * {@code "composite"}, {@code "passthrough"}. The schema allows unknown
         * values (passed through verbatim). Default: unset (the platform default
         * {@code "capability"} applies).
         *
         * @param algorithm the routing algorithm
         * @return this builder
         */
        public Builder algorithm(String algorithm) {
            Objects.requireNonNull(algorithm, "algorithm must not be null");
            this.algorithm = algorithm;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"} (set from the
         * plugin type). Null values are emitted as JSON {@code null}.
         *
         * @param key the field name
         * @param value the field value
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterSwitchyardRouterPlugin} value type.
         *
         * @return the plugin
         */
        public OpenRouterSwitchyardRouterPlugin build() {
            return new OpenRouterSwitchyardRouterPlugin(this);
        }
    }
}
