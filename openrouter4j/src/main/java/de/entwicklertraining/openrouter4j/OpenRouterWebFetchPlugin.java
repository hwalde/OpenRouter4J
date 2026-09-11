package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code web-fetch} plugin: lets the
 * model fetch content from URLs (the retrieval counterpart of the
 * {@code web} search plugin). Emitted into the {@code plugins} request array
 * as {@code {"id": "web-fetch", ...}} with only the explicitly configured
 * fields.
 * <p>
 * JSON field: {@code plugins[].id = "web-fetch"}. Default: no plugin is sent.
 * <p>
 * Trap: fetches count against {@code max_uses}; once exceeded, the tool
 * returns an error to the model. Content longer than
 * {@code max_content_tokens} is truncated.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterWebFetchPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "web-fetch";

    private final Integer maxUses;
    private final Integer maxContentTokens;
    private final List<String> allowedDomains;
    private final List<String> blockedDomains;
    private final Map<String, Object> extraOptions;

    private OpenRouterWebFetchPlugin(Builder builder) {
        this.maxUses = builder.maxUses;
        this.maxContentTokens = builder.maxContentTokens;
        this.allowedDomains = builder.allowedDomains == null ? null : List.copyOf(builder.allowedDomains);
        this.blockedDomains = builder.blockedDomains == null ? null : List.copyOf(builder.blockedDomains);
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code web-fetch} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "web-fetch"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code max_uses} value (maximum number of web
     * fetches per request), or {@code null} when unset.
     */
    public Integer maxUses() {
        return maxUses;
    }

    /**
     * Returns the configured {@code max_content_tokens} value (approximate
     * token limit; longer content is truncated), or {@code null} when unset.
     */
    public Integer maxContentTokens() {
        return maxContentTokens;
    }

    /**
     * Returns the configured {@code allowed_domains} values ("only fetch from
     * these domains"), empty when unset (never {@code null}).
     */
    public List<String> allowedDomains() {
        return allowedDomains == null ? List.of() : allowedDomains;
    }

    /**
     * Returns the configured {@code blocked_domains} values ("never fetch from
     * these domains"), empty when unset (never {@code null}).
     */
    public List<String> blockedDomains() {
        return blockedDomains == null ? List.of() : blockedDomains;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (maxUses != null) {
            json.put("max_uses", maxUses);
        }
        if (maxContentTokens != null) {
            json.put("max_content_tokens", maxContentTokens);
        }
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            json.put("allowed_domains", new JSONArray(allowedDomains));
        }
        if (blockedDomains != null && !blockedDomains.isEmpty()) {
            json.put("blocked_domains", new JSONArray(blockedDomains));
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code web-fetch} plugin. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private Integer maxUses;
        private Integer maxContentTokens;
        private List<String> allowedDomains;
        private List<String> blockedDomains;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code max_uses}: maximum number of web fetches per request;
         * further calls return an error to the model.
         */
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets {@code max_content_tokens}: maximum content length in
         * approximate tokens; content exceeding the limit is truncated.
         */
        public Builder maxContentTokens(Integer maxContentTokens) {
            this.maxContentTokens = maxContentTokens;
            return this;
        }

        /**
         * Sets {@code allowed_domains}: only fetch from these domains.
         */
        public Builder allowedDomains(List<String> domains) {
            this.allowedDomains = domains;
            return this;
        }

        /**
         * Sets {@code blocked_domains}: never fetch from these domains.
         */
        public Builder blockedDomains(List<String> domains) {
            this.blockedDomains = domains;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"}. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterWebFetchPlugin} value type.
         */
        public OpenRouterWebFetchPlugin build() {
            return new OpenRouterWebFetchPlugin(this);
        }
    }
}
