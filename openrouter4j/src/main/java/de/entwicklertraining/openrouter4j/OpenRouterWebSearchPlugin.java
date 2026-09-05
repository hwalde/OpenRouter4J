package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * Typed implementation of the OpenRouter {@code web} plugin (web search).
 * Emitted into the {@code plugins} request array as
 * {@code {"id": "web", ...}} with only the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "web"}. Default: no plugin is sent.
 * <p>
 * Trap: web search results are delivered to the model as tool calls
 * ({@code server_tool_calls}) executed server-side by OpenRouter. Combining
 * this plugin with client-side {@code tools} / {@code tool_choice} means two
 * tool-call flows in one request - the plugin runs independently of the
 * tools you registered.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins/web-search">Web search plugin</a>
 */
public final class OpenRouterWebSearchPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "web";

    private final Boolean enabled;
    private final String engine;
    private final Integer maxResults;
    private final Integer maxUses;
    private final String mode;
    private final String searchPrompt;
    private final List<String> includeDomains;
    private final List<String> excludeDomains;
    private final UserLocation userLocation;

    private OpenRouterWebSearchPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.engine = builder.engine;
        this.maxResults = builder.maxResults;
        this.maxUses = builder.maxUses;
        this.mode = builder.mode;
        this.searchPrompt = builder.searchPrompt;
        this.includeDomains = builder.includeDomains == null ? null : List.copyOf(builder.includeDomains);
        this.excludeDomains = builder.excludeDomains == null ? null : List.copyOf(builder.excludeDomains);
        this.userLocation = builder.userLocation;
    }

    /**
     * Creates a new builder for the {@code web} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String id() {
        return PLUGIN_ID;
    }

    public Boolean enabled() {
        return enabled;
    }

    public String engine() {
        return engine;
    }

    public Integer maxResults() {
        return maxResults;
    }

    public Integer maxUses() {
        return maxUses;
    }

    public String mode() {
        return mode;
    }

    public String searchPrompt() {
        return searchPrompt;
    }

    public List<String> includeDomains() {
        return includeDomains == null ? List.of() : includeDomains;
    }

    public List<String> excludeDomains() {
        return excludeDomains == null ? List.of() : excludeDomains;
    }

    public UserLocation userLocation() {
        return userLocation;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        if (engine != null) {
            json.put("engine", engine);
        }
        if (maxResults != null) {
            json.put("max_results", maxResults);
        }
        if (maxUses != null) {
            json.put("max_uses", maxUses);
        }
        if (mode != null) {
            json.put("mode", mode);
        }
        if (searchPrompt != null) {
            json.put("search_prompt", searchPrompt);
        }
        if (includeDomains != null && !includeDomains.isEmpty()) {
            json.put("include_domains", new JSONArray(includeDomains));
        }
        if (excludeDomains != null && !excludeDomains.isEmpty()) {
            json.put("exclude_domains", new JSONArray(excludeDomains));
        }
        if (userLocation != null) {
            json.put("user_location", userLocation.toJson());
        }
        return json;
    }

    /**
     * Approximate user location for location-biased search results
     * ({@code plugins[].user_location}). Passed through to native providers
     * that support it (e.g. Anthropic); ignored elsewhere.
     */
    public static final class UserLocation {

        private final String city;
        private final String country;
        private final String region;
        private final String timezone;

        private UserLocation(Builder builder) {
            this.city = builder.city;
            this.country = builder.country;
            this.region = builder.region;
            this.timezone = builder.timezone;
        }

        /**
         * Creates a new builder for an approximate user location.
         */
        public static Builder builder() {
            return new Builder();
        }

        public String city() {
            return city;
        }

        public String country() {
            return country;
        }

        public String region() {
            return region;
        }

        public String timezone() {
            return timezone;
        }

        JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", "approximate");
            if (city != null) {
                json.put("city", city);
            }
            if (country != null) {
                json.put("country", country);
            }
            if (region != null) {
                json.put("region", region);
            }
            if (timezone != null) {
                json.put("timezone", timezone);
            }
            return json;
        }

        /** Builder for {@link UserLocation}. */
        public static final class Builder {
            private String city;
            private String country;
            private String region;
            private String timezone;

            private Builder() {
            }

            public Builder city(String city) {
                this.city = city;
                return this;
            }

            public Builder country(String country) {
                this.country = country;
                return this;
            }

            public Builder region(String region) {
                this.region = region;
                return this;
            }

            public Builder timezone(String timezone) {
                this.timezone = timezone;
                return this;
            }

            public UserLocation build() {
                return new UserLocation(this);
            }
        }
    }

    /** Builder for {@link OpenRouterWebSearchPlugin}. */
    public static final class Builder {

        private Boolean enabled;
        private String engine;
        private Integer maxResults;
        private Integer maxUses;
        private String mode;
        private String searchPrompt;
        private List<String> includeDomains;
        private List<String> excludeDomains;
        private UserLocation userLocation;

        private Builder() {
        }

        /**
         * Sets {@code plugins[].enabled}: set to {@code false} to disable the
         * web-search plugin for this request. Default: unset ({@code true} applies).
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets {@code plugins[].engine}: the search engine to use.
         * Documented values: {@code "native"}, {@code "exa"}, {@code "firecrawl"},
         * {@code "parallel"}, {@code "perplexity"}.
         */
        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        /**
         * Sets {@code plugins[].max_results}: the maximum number of search results
         * returned per search call.
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets {@code plugins[].max_uses}: how often the model may invoke web
         * search in a single turn. Passed through to native providers that
         * support it (e.g. Anthropic).
         */
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets {@code plugins[].mode}: engine-native search mode. Documented
         * values: {@code "instant"}, {@code "fast"}, {@code "auto"},
         * {@code "deep-lite"}, {@code "deep"}, {@code "deep-reasoning"},
         * {@code "turbo"}, {@code "basic"}, {@code "advanced"}; modes unsupported
         * by the selected engine are ignored.
         */
        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets {@code plugins[].search_prompt}: the prompt injected into the
         * conversation when search results are attached.
         */
        public Builder searchPrompt(String searchPrompt) {
            this.searchPrompt = searchPrompt;
            return this;
        }

        /**
         * Sets {@code plugins[].include_domains}: restricts search results to
         * these domains. Supports wildcards (e.g. {@code *.substack.com}) and
         * path filtering (e.g. {@code openai.com/blog}).
         */
        public Builder includeDomains(List<String> domains) {
            this.includeDomains = domains;
            return this;
        }

        /**
         * Sets {@code plugins[].exclude_domains}: excludes search results from
         * these domains. Supports wildcards (e.g. {@code *.substack.com}) and
         * path filtering (e.g. {@code openai.com/blog}).
         */
        public Builder excludeDomains(List<String> domains) {
            this.excludeDomains = domains;
            return this;
        }

        /**
         * Sets {@code plugins[].user_location}: approximate user location for
         * location-biased results.
         */
        public Builder userLocation(UserLocation userLocation) {
            this.userLocation = userLocation;
            return this;
        }

        public OpenRouterWebSearchPlugin build() {
            return new OpenRouterWebSearchPlugin(this);
        }
    }
}
