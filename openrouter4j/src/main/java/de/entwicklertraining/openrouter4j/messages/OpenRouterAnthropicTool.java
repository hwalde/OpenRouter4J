package de.entwicklertraining.openrouter4j.messages;

import org.json.JSONObject;

/**
 * One tool definition of the Anthropic Messages API (the {@code tools}
 * request array). A custom tool carries {@code name} and
 * {@code input_schema} (both required by the API) plus the optional
 * {@code description}, {@code defer_loading} and the Anthropic-style
 * {@code cache_control} breakpoint; server tools carry a {@code type}
 * discriminator instead (e.g. {@code web_search_20250305}) and are sent
 * verbatim via {@code OpenRouterMessagesRequest.Builder#addTool(JSONObject)}.
 *
 * <p>Only explicitly configured fields are emitted - an unset option never
 * appears in the tool JSON.
 */
public final class OpenRouterAnthropicTool {

    private final String name;
    private final String description;
    private final JSONObject inputSchema;
    private final Boolean deferLoading;
    private final String cacheControlTtl;

    private OpenRouterAnthropicTool(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.inputSchema = builder.inputSchema == null ? null : new JSONObject(builder.inputSchema.toString());
        this.deferLoading = builder.deferLoading;
        this.cacheControlTtl = builder.cacheControlTtl;
    }

    /** @return the tool name (required by the API) */
    public String name() {
        return name;
    }

    /** @return the tool description, or {@code null} when unset */
    public String description() {
        return description;
    }

    /** @return the JSON schema of the tool input, or {@code null} when unset */
    public JSONObject inputSchema() {
        return inputSchema;
    }

    /** @return whether the tool is deferred until tool search reveals it, or {@code null} when unset */
    public Boolean deferLoading() {
        return deferLoading;
    }

    /**
     * @return the cache_control TTL ({@code 5m} or {@code 1h}); the empty
     *         string means the default TTL was requested via
     *         {@link Builder#cacheControl(String)} with {@code null}, and
     *         {@code null} means no cache_control marker is emitted
     */
    public String cacheControlTtl() {
        return cacheControlTtl;
    }

    /**
     * Builds the JSON object emitted into the {@code tools} array:
     * {@code name} and {@code input_schema} (required),
     * {@code description}, {@code defer_loading} and {@code cache_control}
     * (all omitted when unset). No {@code type} field is emitted - the
     * custom-tool form is the default when the field is absent.
     *
     * @return the tool JSON
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        if (description != null) {
            json.put("description", description);
        }
        if (inputSchema != null) {
            json.put("input_schema", new JSONObject(inputSchema.toString()));
        }
        if (deferLoading != null) {
            json.put("defer_loading", deferLoading);
        }
        if (cacheControlTtl != null) {
            JSONObject cacheControl = new JSONObject();
            cacheControl.put("type", "ephemeral");
            if (!cacheControlTtl.isEmpty()) {
                cacheControl.put("ttl", cacheControlTtl);
            }
            json.put("cache_control", cacheControl);
        }
        return json;
    }

    /**
     * Starting point for building an {@link OpenRouterAnthropicTool}.
     */
    public static final class Builder {

        private String name;
        private String description;
        private JSONObject inputSchema;
        private Boolean deferLoading;
        private String cacheControlTtl;

        /**
         * Sets the required tool {@code name}.
         *
         * @param name the tool name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the optional tool {@code description} - what the tool does;
         * the model reads it to decide when to call the tool.
         *
         * @param description the tool description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the required {@code input_schema} - the JSON schema object
         * describing the tool input (e.g.
         * {@code {"type":"object","properties":{...},"required":[...]}}).
         *
         * @param inputSchema the input JSON schema
         * @return this builder
         */
        public Builder inputSchema(JSONObject inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        /**
         * Sets {@code defer_loading} - withholds the tool from the model
         * until tool search reveals it (requires the
         * {@code openrouter:tool_search} server tool). The key is omitted
         * when unset (API default {@code false}).
         *
         * @param deferLoading the defer flag
         * @return this builder
         */
        public Builder deferLoading(Boolean deferLoading) {
            this.deferLoading = deferLoading;
            return this;
        }

        /**
         * Emits the top-level Anthropic-style {@code cache_control} object
         * ({@code {"type":"ephemeral","ttl":...}}) on the tool, marking the
         * tool as an explicit prompt-cache breakpoint (five-minute default
         * TTL when the ttl is omitted).
         *
         * @param ttl {@code 5m} or {@code 1h}, or {@code null} for the default TTL
         * @return this builder
         */
        public Builder cacheControl(String ttl) {
            this.cacheControlTtl = ttl == null ? "" : ttl;
            return this;
        }

        /**
         * Builds the tool.
         *
         * @return the tool definition
         */
        public OpenRouterAnthropicTool build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("name is required for an Anthropic tool");
            }
            if (inputSchema == null) {
                throw new IllegalStateException("inputSchema is required for an Anthropic tool");
            }
            return new OpenRouterAnthropicTool(this);
        }
    }

    /**
     * Convenience factory for a simple custom tool.
     *
     * @param name the tool name
     * @param description the tool description
     * @param inputSchema the input JSON schema
     * @return the tool definition
     */
    public static OpenRouterAnthropicTool of(String name, String description, JSONObject inputSchema) {
        return new Builder()
                .name(name)
                .description(description)
                .inputSchema(inputSchema)
                .build();
    }
}
