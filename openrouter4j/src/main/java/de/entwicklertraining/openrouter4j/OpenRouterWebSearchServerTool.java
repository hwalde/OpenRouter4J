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
    private final OpenRouterWebSearchPlugin.UserLocation userLocation;
    private final XSearchOptions xSearch;
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
        this.userLocation = builder.userLocation;
        this.xSearch = builder.xSearch;
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
     * Returns the configured {@code parameters.user_location} value
     * (approximate user location for location-aware results), or {@code null}
     * when unset.
     */
    public OpenRouterWebSearchPlugin.UserLocation userLocation() {
        return userLocation;
    }

    /**
     * Returns the configured {@code parameters.x_search} value (X/Twitter
     * search alongside native web search), or {@code null} when unset.
     */
    public XSearchOptions xSearch() {
        return xSearch;
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
        if (userLocation != null) {
            parameters.put("user_location", userLocation.toJson());
        }
        if (xSearch != null) {
            parameters.put("x_search", xSearch.toJson());
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
        private OpenRouterWebSearchPlugin.UserLocation userLocation;
        private XSearchOptions xSearch;
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
         * Sets {@code parameters.user_location}: approximate user location for
         * location-aware results (e.g. local business search). Reuses the
         * {@link OpenRouterWebSearchPlugin.UserLocation} type (the plugin's
         * {@code user_location} has the same wire shape). Emitted only when set.
         * Trap: passed through to native providers only; Exa / Parallel /
         * Firecrawl / Perplexity ignore it.
         *
         * @param userLocation the approximate user location
         * @return this builder
         */
        public Builder userLocation(OpenRouterWebSearchPlugin.UserLocation userLocation) {
            this.userLocation = userLocation;
            return this;
        }

        /**
         * Sets {@code parameters.x_search}: enable X (Twitter) search alongside
         * native web search. Emitted only when set.
         * <p>
         * Trap: X search only applies to providers with native search and is
         * billed separately. {@code allowed_x_handles} and
         * {@code excluded_x_handles} are mutually exclusive - set at most one.
         *
         * @param xSearch the X search options
         * @return this builder
         */
        public Builder xSearch(XSearchOptions xSearch) {
            this.xSearch = xSearch;
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

    /**
     * Typed options for {@code parameters.x_search} - X (Twitter) search
     * alongside native web search ({@code XSearchOptions} in the OpenAPI
     * schema). Only explicitly configured fields are emitted.
     * <p>
     * Trap: X search only applies to providers with native search and is
     * billed separately. {@code allowed_x_handles} and
     * {@code excluded_x_handles} are mutually exclusive - set at most one.
     */
    public static final class XSearchOptions {

        private final List<String> allowedXHandles;
        private final List<String> excludedXHandles;
        private final String fromDate;
        private final String toDate;
        private final Boolean enableImageUnderstanding;
        private final Boolean enableVideoUnderstanding;

        private XSearchOptions(Builder builder) {
            this.allowedXHandles = builder.allowedXHandles == null ? null : List.copyOf(builder.allowedXHandles);
            this.excludedXHandles = builder.excludedXHandles == null ? null : List.copyOf(builder.excludedXHandles);
            this.fromDate = builder.fromDate;
            this.toDate = builder.toDate;
            this.enableImageUnderstanding = builder.enableImageUnderstanding;
            this.enableVideoUnderstanding = builder.enableVideoUnderstanding;
        }

        /**
         * Creates a new builder for X search options.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Returns the configured {@code allowed_x_handles}, empty when unset (never {@code null}). */
        public List<String> allowedXHandles() {
            return allowedXHandles == null ? List.of() : allowedXHandles;
        }

        /** Returns the configured {@code excluded_x_handles}, empty when unset (never {@code null}). */
        public List<String> excludedXHandles() {
            return excludedXHandles == null ? List.of() : excludedXHandles;
        }

        /** Returns the configured {@code from_date} value, or {@code null} when unset. */
        public String fromDate() {
            return fromDate;
        }

        /** Returns the configured {@code to_date} value, or {@code null} when unset. */
        public String toDate() {
            return toDate;
        }

        /** Returns the configured {@code enable_image_understanding} value, or {@code null} when unset. */
        public Boolean enableImageUnderstanding() {
            return enableImageUnderstanding;
        }

        /** Returns the configured {@code enable_video_understanding} value, or {@code null} when unset. */
        public Boolean enableVideoUnderstanding() {
            return enableVideoUnderstanding;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            if (allowedXHandles != null && !allowedXHandles.isEmpty()) {
                JSONArray arr = new JSONArray();
                allowedXHandles.forEach(arr::put);
                json.put("allowed_x_handles", arr);
            }
            if (excludedXHandles != null && !excludedXHandles.isEmpty()) {
                JSONArray arr = new JSONArray();
                excludedXHandles.forEach(arr::put);
                json.put("excluded_x_handles", arr);
            }
            if (fromDate != null) {
                json.put("from_date", fromDate);
            }
            if (toDate != null) {
                json.put("to_date", toDate);
            }
            if (enableImageUnderstanding != null) {
                json.put("enable_image_understanding", enableImageUnderstanding);
            }
            if (enableVideoUnderstanding != null) {
                json.put("enable_video_understanding", enableVideoUnderstanding);
            }
            return json;
        }

        /**
         * Builder for {@link XSearchOptions}. Only explicitly configured
         * fields are emitted.
         */
        public static final class Builder {

            private List<String> allowedXHandles;
            private List<String> excludedXHandles;
            private String fromDate;
            private String toDate;
            private Boolean enableImageUnderstanding;
            private Boolean enableVideoUnderstanding;

            private Builder() {
            }

            /**
             * Sets {@code allowed_x_handles}: restrict X search to these handles
             * (max 20). Mutually exclusive with
             * {@link #excludedXHandles(List)}.
             *
             * @param handles the allowed X handles (without or with {@code @})
             * @return this builder
             */
            public Builder allowedXHandles(List<String> handles) {
                this.allowedXHandles = handles;
                return this;
            }

            /**
             * Sets {@code excluded_x_handles}: exclude these handles from X
             * search (max 20). Mutually exclusive with
             * {@link #allowedXHandles(List)}.
             *
             * @param handles the excluded X handles (without or with {@code @})
             * @return this builder
             */
            public Builder excludedXHandles(List<String> handles) {
                this.excludedXHandles = handles;
                return this;
            }

            /**
             * Sets {@code from_date}: only include X posts from this date
             * (ISO 8601 date, e.g. {@code 2025-01-01}).
             *
             * @param fromDate the start date
             * @return this builder
             */
            public Builder fromDate(String fromDate) {
                this.fromDate = fromDate;
                return this;
            }

            /**
             * Sets {@code to_date}: only include X posts up to this date
             * (ISO 8601 date, e.g. {@code 2025-12-31}).
             *
             * @param toDate the end date
             * @return this builder
             */
            public Builder toDate(String toDate) {
                this.toDate = toDate;
                return this;
            }

            /**
             * Sets {@code enable_image_understanding}: allow the model to
             * understand images in X posts. {@code false} is a set option and
             * is emitted.
             *
             * @param enable whether image understanding is enabled
             * @return this builder
             */
            public Builder enableImageUnderstanding(Boolean enable) {
                this.enableImageUnderstanding = enable;
                return this;
            }

            /**
             * Sets {@code enable_video_understanding}: allow the model to
             * understand videos in X posts. {@code false} is a set option and
             * is emitted.
             *
             * @param enable whether video understanding is enabled
             * @return this builder
             */
            public Builder enableVideoUnderstanding(Boolean enable) {
                this.enableVideoUnderstanding = enable;
                return this;
            }

            /**
             * Builds the X search options.
             *
             * @return the X search options
             */
            public XSearchOptions build() {
                return new XSearchOptions(this);
            }
        }
    }
}
