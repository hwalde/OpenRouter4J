package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code openrouter:web_search} server
 * tool. Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:web_search"}} plus a {@code parameters} object
 * holding only the explicitly configured fields.
 * <p>
 * The tool lets the model search the web for current information; the search is
 * executed server-side by OpenRouter and its results are returned to the model
 * as tool output. The client registers no callback.
 * <p>
 * Trap: the search runs inside OpenRouter's server-tool agent loop, which can
 * call the tool repeatedly. Cap the loop with
 * {@code OpenRouterChatCompletionRequest.Builder#stopServerToolsWhen(OpenRouterStopCondition...)}
 * or a {@code max_tool_calls}-style budget where cost matters.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/web-search">Web search server tool</a>
 */
public final class OpenRouterWebSearchServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:web_search";

    private final String engine;
    private final Integer maxResults;
    private final Integer maxTotalResults;
    private final Integer maxUses;
    private final Integer maxCharacters;
    private final String mode;
    private final String searchContextSize;
    private final List<String> allowedDomains;
    private final List<String> excludedDomains;
    private final Map<String, Object> extraOptions;

    private OpenRouterWebSearchServerTool(Builder builder) {
        this.engine = builder.engine;
        this.maxResults = builder.maxResults;
        this.maxTotalResults = builder.maxTotalResults;
        this.maxUses = builder.maxUses;
        this.maxCharacters = builder.maxCharacters;
        this.mode = builder.mode;
        this.searchContextSize = builder.searchContextSize;
        this.allowedDomains = builder.allowedDomains == null ? null : List.copyOf(builder.allowedDomains);
        this.excludedDomains = builder.excludedDomains == null ? null : List.copyOf(builder.excludedDomains);
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:web_search} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:web_search"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.engine} value
     * ({@code "auto"}, {@code "native"}, {@code "exa"}, {@code "parallel"},
     * {@code "firecrawl"} or {@code "perplexity"}), or {@code null} when unset.
     */
    public String engine() {
        return engine;
    }

    /**
     * Returns the configured {@code parameters.max_results} value, or {@code null} when unset.
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Returns the configured {@code parameters.max_total_results} value, or {@code null} when unset.
     */
    public Integer maxTotalResults() {
        return maxTotalResults;
    }

    /**
     * Returns the configured {@code parameters.max_uses} value, or {@code null} when unset.
     */
    public Integer maxUses() {
        return maxUses;
    }

    /**
     * Returns the configured {@code parameters.max_characters} value, or {@code null} when unset.
     */
    public Integer maxCharacters() {
        return maxCharacters;
    }

    /**
     * Returns the configured engine-native {@code parameters.mode} value, or {@code null} when unset.
     */
    public String mode() {
        return mode;
    }

    /**
     * Returns the configured {@code parameters.search_context_size} value
     * ({@code "low"}, {@code "medium"} or {@code "high"}), or {@code null} when unset.
     */
    public String searchContextSize() {
        return searchContextSize;
    }

    /**
     * Returns the configured {@code parameters.allowed_domains}, empty when unset (never {@code null}).
     */
    public List<String> allowedDomains() {
        return allowedDomains == null ? List.of() : allowedDomains;
    }

    /**
     * Returns the configured {@code parameters.excluded_domains}, empty when unset (never {@code null}).
     */
    public List<String> excludedDomains() {
        return excludedDomains == null ? List.of() : excludedDomains;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:web_search"}} plus a {@code parameters} object
     * when at least one option is set. The {@code parameters} object carries only
     * explicitly configured fields, followed by the verbatim escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (engine != null) {
            parameters.put("engine", engine);
        }
        if (maxResults != null) {
            parameters.put("max_results", maxResults);
        }
        if (maxTotalResults != null) {
            parameters.put("max_total_results", maxTotalResults);
        }
        if (maxUses != null) {
            parameters.put("max_uses", maxUses);
        }
        if (maxCharacters != null) {
            parameters.put("max_characters", maxCharacters);
        }
        if (mode != null) {
            parameters.put("mode", mode);
        }
        if (searchContextSize != null) {
            parameters.put("search_context_size", searchContextSize);
        }
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            allowedDomains.forEach(arr::put);
            parameters.put("allowed_domains", arr);
        }
        if (excludedDomains != null && !excludedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            excludedDomains.forEach(arr::put);
            parameters.put("excluded_domains", arr);
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
     * Builder for the {@code openrouter:web_search} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private String engine;
        private Integer maxResults;
        private Integer maxTotalResults;
        private Integer maxUses;
        private Integer maxCharacters;
        private String mode;
        private String searchContextSize;
        private List<String> allowedDomains;
        private List<String> excludedDomains;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.engine}: {@code "auto"} (default) uses native
         * search if the provider supports it, otherwise Exa; {@code "native"}
         * forces the provider's built-in search; {@code "exa"}, {@code "parallel"},
         * {@code "firecrawl"} (requires BYOK) and {@code "perplexity"} force the
         * respective search API.
         *
         * @param engine one of {@code auto}, {@code native}, {@code exa},
         *               {@code parallel}, {@code firecrawl}, {@code perplexity}
         * @return this builder
         */
        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        /**
         * Sets {@code parameters.max_results}: maximum number of search results
         * per search call (default 5; Perplexity clamps at 20).
         *
         * @param maxResults maximum results per search call
         * @return this builder
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets {@code parameters.max_total_results}: maximum total results across
         * all search calls in one request (default 50) - useful for controlling
         * cost and context size in agentic loops.
         *
         * @param maxTotalResults maximum total results per request
         * @return this builder
         */
        public Builder maxTotalResults(Integer maxTotalResults) {
            this.maxTotalResults = maxTotalResults;
            return this;
        }

        /**
         * Sets {@code parameters.max_uses}: maximum number of web searches the
         * model may perform in one request; further calls return an error result.
         *
         * @param maxUses maximum number of search calls
         * @return this builder
         */
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets {@code parameters.max_characters}: exact maximum content characters
         * per search result (Exa, Parallel, Perplexity; ignored with native
         * provider search and Firecrawl). Takes precedence over
         * {@link #searchContextSize(String)}.
         *
         * @param maxCharacters maximum characters per result
         * @return this builder
         */
        public Builder maxCharacters(Integer maxCharacters) {
            this.maxCharacters = maxCharacters;
            return this;
        }

        /**
         * Sets {@code parameters.mode}: engine-native search mode. Exa supports
         * {@code instant}, {@code fast}, {@code auto}, {@code deep-lite},
         * {@code deep}, {@code deep-reasoning}; Parallel supports {@code turbo},
         * {@code fast}, {@code basic}, {@code advanced}. Unsupported modes are
         * ignored by the engine.
         *
         * @param mode the engine-native search mode
         * @return this builder
         */
        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets {@code parameters.search_context_size}: how much context to
         * retrieve per result ({@code "low"}, {@code "medium"}, {@code "high"}).
         * Applies to Exa, Parallel and Perplexity; ignored with native provider
         * search and Firecrawl.
         *
         * @param searchContextSize one of {@code low}, {@code medium}, {@code high}
         * @return this builder
         */
        public Builder searchContextSize(String searchContextSize) {
            this.searchContextSize = searchContextSize;
            return this;
        }

        /**
         * Sets {@code parameters.allowed_domains}: limit search results to these
         * domains. Cannot be combined with {@link #excludedDomains(List)}.
         *
         * @param domains the allowed domains
         * @return this builder
         */
        public Builder allowedDomains(List<String> domains) {
            this.allowedDomains = domains;
            return this;
        }

        /**
         * Sets {@code parameters.excluded_domains}: exclude search results from
         * these domains. Cannot be combined with {@link #allowedDomains(List)}.
         *
         * @param domains the excluded domains
         * @return this builder
         */
        public Builder excludedDomains(List<String> domains) {
            this.excludedDomains = domains;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet (e.g. a future
         * engine-specific option). Null values are emitted as JSON {@code null}.
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
         * @return the {@code openrouter:web_search} server tool
         */
        public OpenRouterWebSearchServerTool build() {
            return new OpenRouterWebSearchServerTool(this);
        }
    }
}
