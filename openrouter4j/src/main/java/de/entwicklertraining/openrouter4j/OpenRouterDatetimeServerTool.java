package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code openrouter:datetime} server
 * tool. Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:datetime"}} plus a {@code parameters} object when
 * at least one option is set.
 * <p>
 * The tool lets the model query the current date and time; the lookup is
 * executed server-side by OpenRouter. The client registers no callback.
 */
public final class OpenRouterDatetimeServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:datetime";

    private final String timezone;
    private final Map<String, Object> extraOptions;

    private OpenRouterDatetimeServerTool(Builder builder) {
        this.timezone = builder.timezone;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:datetime} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a {@code openrouter:datetime} server tool without configuration
     * (defaults to UTC).
     */
    public static OpenRouterDatetimeServerTool unconfigured() {
        return builder().build();
    }

    /** Returns the server-tool discriminator {@code "openrouter:datetime"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.timezone} value (IANA name),
     * or {@code null} when unset (the API defaults to UTC).
     */
    public String timezone() {
        return timezone;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:datetime"}} plus a {@code parameters} object
     * when at least one option is set. The {@code parameters} object carries only
     * explicitly configured fields, followed by the verbatim escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (timezone != null) {
            parameters.put("timezone", timezone);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            parameters.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }

        if (!parameters.isEmpty()) {
            tool.put("parameters", parameters);
        }
        return tool;
    }

    /**
     * Builder for the {@code openrouter:datetime} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private String timezone;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.timezone}: the IANA timezone name the tool
         * reports the time in (e.g. {@code "America/New_York"}); the API default
         * is UTC.
         *
         * @param timezone the IANA timezone name
         * @return this builder
         */
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet. Null values are
         * emitted as JSON {@code null}.
         *
         * @param key the parameter key
         * @param value the parameter value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the server tool.
         *
         * @return the {@code openrouter:datetime} server tool
         */
        public OpenRouterDatetimeServerTool build() {
            return new OpenRouterDatetimeServerTool(this);
        }
    }
}
