package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code openrouter:tool_search} server
 * tool. Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:tool_search"}} plus a {@code parameters} object
 * holding only the explicitly configured fields.
 * <p>
 * The tool finds function tools marked with {@code defer_loading} (see
 * {@link OpenRouterToolDefinition.Builder#deferLoading(Boolean)}) and makes them
 * callable. This keeps prompts small for large tool catalogs: the deferred tools
 * are withheld from the model until the model searches the catalog, then only the
 * matching tools become callable.
 *
 * <p><b>Trap:</b> OpenRouter serves {@code openrouter:tool_search} only through
 * the Responses API and the Anthropic Messages API. Requesting it on the Chat
 * Completions API (the only endpoint this library currently implements) fails
 * with HTTP 400: {@code "Tool 'openrouter:tool_search' is not available for the
 * 'chat-completions' API. It is only supported for: responses, anthropic-messages."}.
 * On Chat Completions, use {@link OpenRouterToolDefinition.Builder#deferLoading(Boolean)}
 * <em>without</em> this server tool instead - the request then routes to a
 * provider whose gateway expands deferred tools itself (Anthropic models and
 * Anthropic-compatible endpoints only; other models return a 400). This class is
 * the typed surface for the schema-defined tool so it is ready the moment the
 * library speaks the other APIs; do not send it through
 * {@code OpenRouterChatCompletionRequest.Builder#serverTools(OpenRouterServerTool...)}.
 *
 * <p>API constraint of the tool-search flow: at least one tool must remain
 * non-deferred. Also note {@code tool_choice} conflicts with deferral - it must
 * be omitted or left at the default {@code "auto"}, otherwise the request fails
 * with a 400 (the docs also accept the {@code allowed_tools} form there, which
 * this library does not offer).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/tool-search">Tool search server tool</a>
 */
public final class OpenRouterToolSearchServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:tool_search";

    private final Integer maxResults;
    private final Map<String, Object> extraOptions;

    private OpenRouterToolSearchServerTool(Builder builder) {
        this.maxResults = builder.maxResults;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:tool_search} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:tool_search"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.max_results} value - the maximum
     * number of tools returned by one search (API default 5) - or {@code null}
     * when unset.
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:tool_search"}} plus a {@code parameters} object
     * when at least one option is set. The {@code parameters} object carries only
     * explicitly configured fields, followed by the verbatim escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (maxResults != null) {
            parameters.put("max_results", maxResults);
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
     * Builder for the {@code openrouter:tool_search} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private Integer maxResults;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.max_results}: maximum tools returned by one
         * search. Defaults to 5.
         *
         * @param maxResults maximum tools returned per search
         * @return this builder
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
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
         * @return the {@code openrouter:tool_search} server tool
         */
        public OpenRouterToolSearchServerTool build() {
            return new OpenRouterToolSearchServerTool(this);
        }
    }
}
