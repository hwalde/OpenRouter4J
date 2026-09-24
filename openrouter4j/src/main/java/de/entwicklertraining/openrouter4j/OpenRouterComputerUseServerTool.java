package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenAI {@code computer_use_preview} server tool:
 * lets the model drive a computer (screenshot / click / type style tool calls).
 * <p>
 * Emitted as {@code {"type": "computer_use_preview", "display_width": ...,
 * "display_height": ..., "environment": ...}} plus the verbatim escape-hatch
 * options - flat fields at the top level of the tool object (this is an
 * OpenAI-native tool type, not an {@code openrouter:*} namespace tool with a
 * {@code parameters} wrapper).
 * <p>
 * {@code display_width}, {@code display_height} and {@code environment} are
 * required by the schema; a request missing any of them is rejected loudly at
 * {@code build()}. {@code environment} is a free-form string (the schema allows
 * unknown values); the documented ones are {@code windows}, {@code mac},
 * {@code linux}, {@code ubuntu} and {@code browser}.
 * <p>
 * <strong>Schema surface:</strong> the published schema declares
 * {@code ComputerUseServerTool} on the Responses request's {@code tools} array
 * (and on the mid-input {@code additional_tools} item). The chat-completions
 * {@code tools} union does not list it - sending it through
 * {@code serverTools(...)} is an escape-hatch use at the caller's risk.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">Server tools</a>
 */
public final class OpenRouterComputerUseServerTool implements OpenRouterServerTool {

    /** The tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "computer_use_preview";

    private final Integer displayWidth;
    private final Integer displayHeight;
    private final String environment;
    private final Map<String, Object> extraOptions;

    private OpenRouterComputerUseServerTool(Builder builder) {
        this.displayWidth = builder.displayWidth;
        this.displayHeight = builder.displayHeight;
        this.environment = builder.environment;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code computer_use_preview} tool.
     * {@code displayWidth}, {@code displayHeight} and {@code environment} are
     * required - set them before {@code build()}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a computer-use tool with the required display size and
     * environment already set.
     *
     * @param displayWidth the display width in pixels (required, positive)
     * @param displayHeight the display height in pixels (required, positive)
     * @param environment the environment kind - {@code windows}, {@code mac},
     *                    {@code linux}, {@code ubuntu} or {@code browser}
     *                    (free-form per the schema)
     * @return a new builder with the three required fields set
     */
    public static Builder builder(int displayWidth, int displayHeight, String environment) {
        return new Builder()
                .displayWidth(displayWidth)
                .displayHeight(displayHeight)
                .environment(environment);
    }

    /** Returns the tool discriminator {@code "computer_use_preview"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /** Returns the configured {@code display_width}, or {@code null} when unset. */
    public Integer displayWidth() {
        return displayWidth;
    }

    /** Returns the configured {@code display_height}, or {@code null} when unset. */
    public Integer displayHeight() {
        return displayHeight;
    }

    /** Returns the configured {@code environment}, or {@code null} when unset. */
    public String environment() {
        return environment;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "computer_use_preview", "display_width": ...,
     * "display_height": ..., "environment": ...}} plus the verbatim
     * escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);
        tool.put("display_width", displayWidth);
        tool.put("display_height", displayHeight);
        tool.put("environment", environment);
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for {@link OpenRouterComputerUseServerTool}. A verbatim
     * {@link #option(String, Object)} escape hatch covers keys this library
     * does not type.
     */
    public static final class Builder {

        private Integer displayWidth;
        private Integer displayHeight;
        private String environment;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code display_width}: the display width in pixels.
         *
         * @param displayWidth the display width (required, positive)
         * @return this builder
         */
        public Builder displayWidth(int displayWidth) {
            if (displayWidth <= 0) {
                throw new IllegalArgumentException("displayWidth must be positive, was: " + displayWidth);
            }
            this.displayWidth = displayWidth;
            return this;
        }

        /**
         * Sets {@code display_height}: the display height in pixels.
         *
         * @param displayHeight the display height (required, positive)
         * @return this builder
         */
        public Builder displayHeight(int displayHeight) {
            if (displayHeight <= 0) {
                throw new IllegalArgumentException("displayHeight must be positive, was: " + displayHeight);
            }
            this.displayHeight = displayHeight;
            return this;
        }

        /**
         * Sets {@code environment}: which kind of computer the model drives.
         * Documented values: {@code windows}, {@code mac}, {@code linux},
         * {@code ubuntu}, {@code browser}. Accepted verbatim (the schema allows
         * unknown values).
         *
         * @param environment the environment kind (required, not blank)
         * @return this builder
         */
        public Builder environment(String environment) {
            Objects.requireNonNull(environment, "environment must not be null");
            if (environment.isBlank()) {
                throw new IllegalArgumentException("environment must not be blank");
            }
            this.environment = environment;
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type. Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"}, {@code "display_width"},
         *            {@code "display_height"} or {@code "environment"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key) || "display_width".equals(key)
                    || "display_height".equals(key) || "environment".equals(key)) {
                throw new IllegalArgumentException(
                        "The '" + key + "' field is set from the typed API and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the computer-use tool. Rejects a tool missing any of the
         * three required fields loudly - the schema requires them.
         *
         * @return the computer-use tool
         */
        public OpenRouterComputerUseServerTool build() {
            if (displayWidth == null || displayHeight == null || environment == null) {
                throw new IllegalStateException(
                        "displayWidth, displayHeight and environment are required - set them before build()");
            }
            return new OpenRouterComputerUseServerTool(this);
        }
    }
}
