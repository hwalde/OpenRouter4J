package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:files} server tool: read,
 * write, edit and list workspace files via the Files API. Emitted into the
 * {@code tools} request array as {@code {"type": "openrouter:files"}} (the
 * API schema defines an empty {@code parameters} object, so none is emitted
 * unless escape-hatch options are set).
 * <p>
 * Trap: requires an authenticated request; the files come from the API key's
 * workspace (or the default workspace for keys without one).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">Server tools</a>
 */
public final class OpenRouterFilesServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:files";

    private final Map<String, Object> extraOptions;

    private OpenRouterFilesServerTool(Builder builder) {
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates the {@code openrouter:files} server tool with no extra options.
     */
    public OpenRouterFilesServerTool() {
        this(new Builder());
    }

    /**
     * Creates a new builder for the {@code openrouter:files} server tool (the
     * schema defines no configuration keys, so the tool is usually built with
     * the no-arg constructor).
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:files"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:files"}} plus a {@code parameters} object
     * when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            parameters.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }

        if (!parameters.isEmpty()) {
            tool.put("parameters", parameters);
        }
        return tool;
    }

    /**
     * Builder for the {@code openrouter:files} server tool. The schema defines
     * no configuration keys; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys OpenRouter may add later.
     */
    public static final class Builder {

        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterFilesServerTool}.
         */
        public OpenRouterFilesServerTool build() {
            return new OpenRouterFilesServerTool(this);
        }
    }
}
