package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:apply_patch} server tool:
 * validates V4A diff patches for file operations (create, update, delete).
 * Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:apply_patch"}} plus a {@code parameters} object
 * when an option is set.
 * <p>
 * Trap: this server-tool type is restricted to the Responses API - sending it
 * on a Chat Completions request returns a 400. With native passthrough the
 * diff streams incrementally; with the OpenRouter HITL validator it is
 * buffered for atomic delivery.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/apply-patch">Apply patch server tool</a>
 */
public final class OpenRouterApplyPatchServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:apply_patch";

    private final String engine;
    private final Map<String, Object> extraOptions;

    private OpenRouterApplyPatchServerTool(Builder builder) {
        this.engine = builder.engine;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:apply_patch} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:apply_patch"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.engine} value
     * ({@code "auto"}, {@code "native"} or {@code "openrouter"}), or
     * {@code null} when unset.
     */
    public String engine() {
        return engine;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:apply_patch"}} plus a {@code parameters}
     * object when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (engine != null) {
            parameters.put("engine", engine);
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
     * Builder for the {@code openrouter:apply_patch} server tool. Only
     * explicitly configured fields are emitted; a verbatim
     * {@link #option(String, Object)} escape hatch covers configuration keys
     * this library does not know yet.
     */
    public static final class Builder {

        private String engine;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.engine}: {@code "auto"} (default) uses native
         * passthrough when the endpoint advertises native apply_patch support,
         * otherwise falls back to OpenRouter's HITL validator; {@code "native"}
         * forces native passthrough (falls back to HITL when unsupported);
         * {@code "openrouter"} always runs the HITL validator.
         */
        public Builder engine(String engine) {
            this.engine = engine;
            return this;
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
         * Builds the {@link OpenRouterApplyPatchServerTool}.
         */
        public OpenRouterApplyPatchServerTool build() {
            return new OpenRouterApplyPatchServerTool(this);
        }
    }
}
