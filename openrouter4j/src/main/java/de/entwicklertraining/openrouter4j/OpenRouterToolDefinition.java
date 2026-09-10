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
    private final Boolean deferLoading;
    private final String cacheControlType;
    private final String cacheControlTtl;

    private OpenRouterToolDefinition(
            String name,
            String description,
            JSONObject parameters,
            OpenRouterToolsCallback callback,
            Boolean strict,
            Boolean deferLoading,
            String cacheControlType,
            String cacheControlTtl
    ) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.callback = callback;
        this.strict = strict;
        this.deferLoading = deferLoading;
        this.cacheControlType = cacheControlType;
        this.cacheControlTtl = cacheControlTtl;
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
     * The {@code function.defer_loading} flag, or {@code null} when unset (the key
     * is omitted from the request).
     */
    public Boolean deferLoading() {
        return deferLoading;
    }

    /**
     * The tool-level {@code cache_control.type} value ({@code "ephemeral"}), or
     * {@code null} when unset (the key is omitted from the request).
     */
    public String cacheControlType() {
        return cacheControlType;
    }

    /**
     * The tool-level {@code cache_control.ttl} value, or {@code null} when unset.
     */
    public String cacheControlTtl() {
        return cacheControlTtl;
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
        if (deferLoading != null) {
            function.put("defer_loading", deferLoading);
        }

        JSONObject tool = new JSONObject();
        tool.put("type", "function");
        tool.put("function", function);
        if (cacheControlType != null) {
            JSONObject cacheControl = new JSONObject();
            cacheControl.put("type", cacheControlType);
            if (cacheControlTtl != null) {
                cacheControl.put("ttl", cacheControlTtl);
            }
            tool.put("cache_control", cacheControl);
        }
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
        private Boolean deferLoading;
        private String cacheControlType;
        private String cacheControlTtl;

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

        /**
         * Sets {@code function.defer_loading}: withholds this tool from the model
         * until it is revealed by tool search. Keeps prompts small for large tool
         * catalogs (dozens or hundreds of tools): the model first searches the
         * catalog, then only the matching tools become callable.
         * <p>
         * JSON field: {@code function.defer_loading}. Default: unset (the key is
         * not sent; the API default is {@code false}).
         * <p>
         * How deferral is expanded depends on the request shape: <b>without</b>
         * the {@code openrouter:tool_search} server tool, the request routes to a
         * provider whose gateway expands deferred tools itself - on Chat
         * Completions that provider-managed path works on Anthropic models and
         * Anthropic-compatible endpoints only (other models return a 400).
         * <b>With</b> {@code openrouter:tool_search}, OpenRouter manages deferral
         * itself and it works on any model - but that server tool is only served
         * by the Responses and Anthropic Messages APIs (see
         * {@link OpenRouterToolSearchServerTool}), not by the Chat Completions
         * endpoint this library implements. Constraint in either case: at least
         * one tool must remain non-deferred. Also note {@code tool_choice}
         * conflicts with deferral on the tool-search path (omit it or leave the
         * default {@code "auto"}, otherwise the request fails with a 400).
         *
         * @param deferLoading {@code Boolean.TRUE} to withhold the tool until revealed by tool search
         * @return this builder
         * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/tool-search">Tool search server tool</a>
         */
        public Builder deferLoading(Boolean deferLoading) {
            this.deferLoading = deferLoading;
            return this;
        }

        /**
         * Marks this tool as an explicit prompt-cache breakpoint with the
         * tool-level {@code cache_control: {"type":"ephemeral"}} object (default
         * 5-minute TTL) - the same marker that content parts carry, but on the
         * tool object itself. A big tool catalog is the canonical use: the tools
         * block is often the largest stable part of a tool-heavy prompt, so
         * marking it keeps the cache boundary stable across turns.
         * <p>
         * JSON field: {@code tools[].cache_control}. Default: unset (the key is
         * not sent). Distinct from the request-root {@code cache_control}
         * ({@code OpenRouterChatCompletionRequest.Builder#cacheControl()}), which
         * enables <em>automatic</em> caching - per-block markers are the
         * fine-grained alternative. Per the docs, explicit breakpoints are
         * limited to four per request and should be reserved for large stable
         * blocks.
         * <p>
         * Trap: the marker works on Anthropic-style caching providers; when
         * routed to an OpenAI explicit-caching model, OpenRouter converts it to
         * a {@code prompt_cache_breakpoint} automatically (TTLs are not
         * translated toward OpenAI).
         *
         * @return this builder
         * @see <a href="https://openrouter.ai/docs/guides/best-practices/prompt-caching">Prompt caching</a>
         */
        public Builder cacheControl() {
            this.cacheControlType = "ephemeral";
            return this;
        }

        /**
         * Marks this tool as an explicit prompt-cache breakpoint with an explicit
         * TTL - see {@link #cacheControl()}. The API supports {@code "5m"}
         * (default) and {@code "1h"}; the 1-hour TTL costs more for cache writes
         * but keeps the cache warm across longer sessions.
         *
         * @param ttl the cache TTL (e.g. {@code "5m"}, {@code "1h"})
         * @return this builder
         */
        public Builder cacheControl(String ttl) {
            this.cacheControlType = "ephemeral";
            this.cacheControlTtl = ttl;
            return this;
        }

        public OpenRouterToolDefinition build() {
            if (!properties.isEmpty()) {
                schema.put("properties", properties);
            }
            if (!required.isEmpty()) {
                schema.put("required", required);
            }

            return new OpenRouterToolDefinition(name, description, schema, callback, strict, deferLoading, cacheControlType, cacheControlTtl);
        }
    }
}
