package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Adapted for OpenRouter's function-calling style.
 * OpenRouter follows the OpenAI format with tools array containing function definitions.
 * Each tool has "type": "function" and a "function" object with "name", "description", "parameters".
 */
public final class OpenRouterToolDefinition {

    private final String name;
    private final String description;
    private final JSONObject parameters;
    private final OpenRouterToolsCallback callback;
    private final Boolean strict;

    private OpenRouterToolDefinition(
            String name,
            String description,
            JSONObject parameters,
            OpenRouterToolsCallback callback,
            Boolean strict
    ) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.callback = callback;
        this.strict = strict;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public JSONObject parameters() {
        return parameters;
    }

    public OpenRouterToolsCallback callback() {
        return callback;
    }

    /**
     * The {@code function.strict} flag ("Enable strict schema adherence"), or
     * {@code null} when unset (the key is omitted from the request).
     */
    public Boolean strict() {
        return strict;
    }

    /**
     * OpenRouter expects tools in this format:
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "...",
     *     "description": "...",
     *     "parameters": {...}
     *   }
     * }
     */
    public JSONObject toJson() {
        JSONObject function = new JSONObject();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);
        if (strict != null) {
            function.put("strict", strict);
        }

        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {

        private final String name;
        private String description;
        private final JSONObject schema = new JSONObject();
        private final JSONObject properties = new JSONObject();
        private final JSONArray required = new JSONArray();
        private OpenRouterToolsCallback callback;
        private Boolean strict;

        private Builder(String name) {
            this.name = name;
            schema.put("type", "object");
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder parameter(String paramName, OpenRouterJsonSchema paramSchema, boolean requiredField) {
            properties.put(paramName, paramSchema.toJson());
            if (requiredField) {
                required.put(paramName);
            }
            return this;
        }

        public Builder callback(OpenRouterToolsCallback cb) {
            this.callback = cb;
            return this;
        }

        /**
         * Sets {@code function.strict} ("Enable strict schema adherence"): with
         * {@code true}, the model's tool arguments must adhere exactly to the
         * declared parameters schema - protection against malformed JSON arguments
         * on providers that support it.
         * <p>
         * JSON field: {@code function.strict}. Default: unset (the key is not sent;
         * the API default is {@code false}). This is the <em>tool</em> {@code strict},
         * distinct from {@code response_format.json_schema.strict} of structured
         * outputs.
         * <p>
         * Note: like every tool, a strict tool can still be silently unsupported by
         * individual endpoints - combine with
         * {@code OpenRouterChatCompletionRequest.Builder#requireParameters(Boolean)}
         * to route only to endpoints that support all request parameters.
         *
         * @param strict {@code Boolean.TRUE} to request strict schema adherence for the tool arguments
         * @return This builder instance
         */
        public Builder strict(Boolean strict) {
            this.strict = strict;
            return this;
        }

        public OpenRouterToolDefinition build() {
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", required);
            }

            return new OpenRouterToolDefinition(name, description, schema, callback, strict);
        }
    }
}
