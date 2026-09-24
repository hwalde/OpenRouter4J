package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenAI Responses-syntax <em>web search shorthand</em>
 * tool that the chat-completions {@code tools} array accepts and that OpenRouter
 * converts automatically to {@code openrouter:web_search}.
 * <p>
 * Emitted verbatim as {@code {"type": "web_search_preview"}} (or one of the other
 * documented {@code type} values) plus the explicitly configured fields at the
 * <em>top level</em> of the tool object - this is OpenAI Responses syntax and is
 * deliberately <em>not</em> wrapped in a {@code parameters} object. The library
 * never converts the shorthand; the conversion is the API's job.
 * <p>
 * {@link OpenRouterWebSearchServerTool} ({@code openrouter:web_search}) is the
 * canonical, strictly more expressive form of the same capability - prefer it for
 * new code. The shorthand exists for callers porting OpenAI Responses-syntax
 * requests verbatim.
 * <p>
 * Traps: {@code allowed_domains} and {@code excluded_domains} are mutually
 * exclusive per the schema - the library emits both verbatim when both are set
 * (same convention as {@link OpenRouterWebSearchServerTool}) and leaves the
 * rejection to the API. {@code max_characters} takes precedence over
 * {@code search_context_size} when both are set. The published schema also
 * accepts a nested {@code parameters} object ({@code WebSearchConfig}) carrying
 * the same search options - send either the flat form or {@code parameters}, not
 * both; this library types the flat form and leaves {@code parameters} to
 * {@link Builder#option(String, Object)}.
 *
 * @see OpenRouterWebSearchServerTool
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/web-search">Web search server tool</a>
 */
public final class OpenRouterWebSearchShorthandTool implements OpenRouterServerTool {

    /** The default tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "web_search_preview";

    private final String type;
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
    private final OpenRouterWebSearchServerTool.XSearchOptions xSearch;
    private final Map<String, Object> extraOptions;

    private OpenRouterWebSearchShorthandTool(Builder builder) {
        this.type = builder.type;
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
     * Creates a new builder for the web search shorthand tool
     * (default {@code type}: {@code "web_search_preview"}).
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder for the web search shorthand tool with the given
     * {@code type} discriminator.
     *
     * @param type one of {@code web_search}, {@code web_search_preview},
     *             {@code web_search_preview_2025_03_11}, {@code web_search_2025_08_26}
     *             (unknown values are accepted verbatim as an escape hatch)
     * @return a new builder
     */
    public static Builder builder(String type) {
        return new Builder().type(type);
    }

    /** Returns the tool discriminator emitted as {@code tools[].type}. */
    @Override
    public String type() {
        return type;
    }

    /**
     * Returns the configured {@code engine} value
     * ({@code "auto"}, {@code "native"}, {@code "exa"}, {@code "parallel"},
     * {@code "firecrawl"} or {@code "perplexity"}), or {@code null} when unset.
     */
    public String engine() {
        return engine;
    }

    /** Returns the configured {@code max_results} value, or {@code null} when unset. */
    public Integer maxResults() {
        return maxResults;
    }

    /** Returns the configured {@code max_total_results} value, or {@code null} when unset. */
    public Integer maxTotalResults() {
        return maxTotalResults;
    }

    /** Returns the configured {@code max_uses} value, or {@code null} when unset. */
    public Integer maxUses() {
        return maxUses;
    }

    /** Returns the configured {@code max_characters} value, or {@code null} when unset. */
    public Integer maxCharacters() {
        return maxCharacters;
    }

    /** Returns the configured engine-native {@code mode} value, or {@code null} when unset. */
    public String mode() {
        return mode;
    }

    /**
     * Returns the configured {@code search_context_size} value
     * ({@code "low"}, {@code "medium"} or {@code "high"}), or {@code null} when unset.
     */
    public String searchContextSize() {
        return searchContextSize;
    }

    /** Returns the configured {@code allowed_domains}, empty when unset (never {@code null}). */
    public List<String> allowedDomains() {
        return allowedDomains == null ? List.of() : allowedDomains;
    }

    /** Returns the configured {@code excluded_domains}, empty when unset (never {@code null}). */
    public List<String> excludedDomains() {
        return excludedDomains == null ? List.of() : excludedDomains;
    }

    /** Returns the configured {@code user_location} value, or {@code null} when unset. */
    public OpenRouterWebSearchPlugin.UserLocation userLocation() {
        return userLocation;
    }

    /** Returns the configured {@code x_search} value, or {@code null} when unset. */
    public OpenRouterWebSearchServerTool.XSearchOptions xSearch() {
        return xSearch;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": ...}} plus the explicitly configured fields at the top
     * level (OpenAI Responses syntax - no {@code parameters} wrapper), followed
     * by the verbatim escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", type);
        if (engine != null) {
            tool.put("engine", engine);
        }
        if (maxResults != null) {
            tool.put("max_results", maxResults);
        }
        if (maxTotalResults != null) {
            tool.put("max_total_results", maxTotalResults);
        }
        if (maxUses != null) {
            tool.put("max_uses", maxUses);
        }
        if (maxCharacters != null) {
            tool.put("max_characters", maxCharacters);
        }
        if (mode != null) {
            tool.put("mode", mode);
        }
        if (searchContextSize != null) {
            tool.put("search_context_size", searchContextSize);
        }
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            allowedDomains.forEach(arr::put);
            tool.put("allowed_domains", arr);
        }
        if (excludedDomains != null && !excludedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            excludedDomains.forEach(arr::put);
            tool.put("excluded_domains", arr);
        }
        if (userLocation != null) {
            tool.put("user_location", userLocation.toJson());
        }
        if (xSearch != null) {
            tool.put("x_search", xSearch.toJson());
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for the web search shorthand tool. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape hatch
     * covers keys this library does not type (including the nested
     * {@code parameters} object of {@code WebSearchConfig}).
     */
    public static final class Builder {

        private String type = TOOL_TYPE;
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
        private OpenRouterWebSearchServerTool.XSearchOptions xSearch;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets the {@code type} discriminator. Documented values:
         * {@code web_search}, {@code web_search_preview} (default),
         * {@code web_search_preview_2025_03_11}, {@code web_search_2025_08_26}.
         * Unknown values are accepted verbatim (the schema allows them).
         *
         * @param type the tool type discriminator
         * @return this builder
         */
        public Builder type(String type) {
            this.type = Objects.requireNonNull(type, "type must not be null");
            return this;
        }

        /**
         * Sets {@code engine}: {@code "auto"} (default) uses native search if
         * the provider supports it, otherwise Exa; {@code "native"} forces the
         * provider's built-in search; {@code "exa"}, {@code "parallel"},
         * {@code "firecrawl"} (requires BYOK) and {@code "perplexity"} force
         * the respective search API.
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
         * Sets {@code max_results}: maximum number of search results per search
         * call (default 5; Perplexity clamps at 20).
         *
         * @param maxResults maximum results per search call
         * @return this builder
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets {@code max_total_results}: maximum total results across all
         * search calls in one request (default 50).
         *
         * @param maxTotalResults maximum total results per request
         * @return this builder
         */
        public Builder maxTotalResults(Integer maxTotalResults) {
            this.maxTotalResults = maxTotalResults;
            return this;
        }

        /**
         * Sets {@code max_uses}: maximum number of web searches the model may
         * perform in one request; further calls return an error result.
         *
         * @param maxUses maximum number of search calls
         * @return this builder
         */
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets {@code max_characters}: exact maximum content characters per
         * search result (Exa, Parallel, Perplexity). Takes precedence over
         * {@link #searchContextSize(String)} when both are set.
         *
         * @param maxCharacters maximum characters per result
         * @return this builder
         */
        public Builder maxCharacters(Integer maxCharacters) {
            this.maxCharacters = maxCharacters;
            return this;
        }

        /**
         * Sets {@code mode}: engine-native search mode. Exa supports
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
         * Sets {@code search_context_size}: how much context to retrieve per
         * result ({@code "low"}, {@code "medium"}, {@code "high"}).
         *
         * @param searchContextSize one of {@code low}, {@code medium}, {@code high}
         * @return this builder
         */
        public Builder searchContextSize(String searchContextSize) {
            this.searchContextSize = searchContextSize;
            return this;
        }

        /**
         * Sets {@code allowed_domains}: limit search results to these domains.
         * Cannot be combined with {@link #excludedDomains(List)} - the library
         * emits both verbatim when both are set (same convention as
         * {@link OpenRouterWebSearchServerTool}) and leaves the rejection to
         * the API. An empty list omits the field.
         *
         * @param domains the allowed domains
         * @return this builder
         */
        public Builder allowedDomains(List<String> domains) {
            this.allowedDomains = domains;
            return this;
        }

        /**
         * Sets {@code excluded_domains}: exclude search results from these
         * domains. Cannot be combined with {@link #allowedDomains(List)} - the
         * library emits both verbatim when both are set (same convention as
         * {@link OpenRouterWebSearchServerTool}) and leaves the rejection to
         * the API. An empty list omits the field.
         *
         * @param domains the excluded domains
         * @return this builder
         */
        public Builder excludedDomains(List<String> domains) {
            this.excludedDomains = domains;
            return this;
        }

        /**
         * Sets {@code user_location}: approximate user location for
         * location-aware results. Reuses the
         * {@link OpenRouterWebSearchPlugin.UserLocation} type (same wire shape).
         * Trap: passed through to native providers only.
         *
         * @param userLocation the approximate user location
         * @return this builder
         */
        public Builder userLocation(OpenRouterWebSearchPlugin.UserLocation userLocation) {
            this.userLocation = userLocation;
            return this;
        }

        /**
         * Sets {@code x_search}: enable X (Twitter) search alongside native web
         * search. Reuses {@link OpenRouterWebSearchServerTool.XSearchOptions}
         * (same wire shape). Trap: only used with native provider search on
         * SpaceXAI (Grok) models; billed separately. {@code allowed_x_handles}
         * and {@code excluded_x_handles} are mutually exclusive.
         *
         * @param xSearch the X search options
         * @return this builder
         */
        public Builder xSearch(OpenRouterWebSearchServerTool.XSearchOptions xSearch) {
            this.xSearch = xSearch;
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type (including the nested {@code parameters} object of
         * {@code WebSearchConfig}). Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key)) {
                throw new IllegalArgumentException(
                        "The 'type' field is set via type(...) and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the web search shorthand tool.
         *
         * @return the shorthand tool
         */
        public OpenRouterWebSearchShorthandTool build() {
            return new OpenRouterWebSearchShorthandTool(this);
        }
    }
}
