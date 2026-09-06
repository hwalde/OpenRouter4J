package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code openrouter:web_fetch} server
 * tool. Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:web_fetch"}} plus a {@code parameters} object
 * holding only the explicitly configured fields.
 * <p>
 * The tool lets the model fetch full content from a URL (web page or PDF); the
 * fetch is executed server-side by OpenRouter. The client registers no callback.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
 */
public final class OpenRouterWebFetchServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:web_fetch";

    private final Integer maxUses;
    private final Integer maxContentTokens;
    private final List<String> allowedDomains;
    private final List<String> blockedDomains;
    private final String engine;
    private final Map<String, Object> extraOptions;

    private OpenRouterWebFetchServerTool(Builder builder) {
        this.maxUses = builder.maxUses;
        this.maxContentTokens = builder.maxContentTokens;
        this.allowedDomains = builder.allowedDomains == null ? null : List.copyOf(builder.allowedDomains);
        this.blockedDomains = builder.blockedDomains == null ? null : List.copyOf(builder.blockedDomains);
        this.engine = builder.engine;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:web_fetch} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:web_fetch"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.max_uses} value, or {@code null} when unset.
     */
    public Integer maxUses() {
        return maxUses;
    }

    /**
     * Returns the configured {@code parameters.max_content_tokens} value, or {@code null} when unset.
     */
    public Integer maxContentTokens() {
        return maxContentTokens;
    }

    /**
     * Returns the configured {@code parameters.allowed_domains}, empty when unset (never {@code null}).
     */
    public List<String> allowedDomains() {
        return allowedDomains == null ? List.of() : allowedDomains;
    }

    /**
     * Returns the configured {@code parameters.blocked_domains}, empty when unset (never {@code null}).
     */
    public List<String> blockedDomains() {
        return blockedDomains == null ? List.of() : blockedDomains;
    }

    /**
     * Returns the configured {@code parameters.engine} value, or {@code null} when unset.
     */
    public String engine() {
        return engine;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:web_fetch"}} plus a {@code parameters} object
     * when at least one option is set. The {@code parameters} object carries only
     * explicitly configured fields, followed by the verbatim escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (maxUses != null) {
            parameters.put("max_uses", maxUses);
        }
        if (maxContentTokens != null) {
            parameters.put("max_content_tokens", maxContentTokens);
        }
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            allowedDomains.forEach(arr::put);
            parameters.put("allowed_domains", arr);
        }
        if (blockedDomains != null && !blockedDomains.isEmpty()) {
            JSONArray arr = new JSONArray();
            blockedDomains.forEach(arr::put);
            parameters.put("blocked_domains", arr);
        }
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
     * Builder for the {@code openrouter:web_fetch} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private Integer maxUses;
        private Integer maxContentTokens;
        private List<String> allowedDomains;
        private List<String> blockedDomains;
        private String engine;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.max_uses}: maximum number of web fetches per
         * request; once exceeded, the tool returns an error.
         *
         * @param maxUses maximum number of fetch calls
         * @return this builder
         */
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        /**
         * Sets {@code parameters.max_content_tokens}: maximum content length in
         * approximate tokens; content exceeding the limit is truncated.
         *
         * @param maxContentTokens maximum content length in tokens
         * @return this builder
         */
        public Builder maxContentTokens(Integer maxContentTokens) {
            this.maxContentTokens = maxContentTokens;
            return this;
        }

        /**
         * Sets {@code parameters.allowed_domains}: only fetch from these domains.
         *
         * @param domains the allowed domains
         * @return this builder
         */
        public Builder allowedDomains(List<String> domains) {
            this.allowedDomains = domains;
            return this;
        }

        /**
         * Sets {@code parameters.blocked_domains}: never fetch from these domains.
         *
         * @param domains the blocked domains
         * @return this builder
         */
        public Builder blockedDomains(List<String> domains) {
            this.blockedDomains = domains;
            return this;
        }

        /**
         * Sets {@code parameters.engine}: which fetch engine to use (the API
         * decides the documented values; e.g. the Exa fetch engine).
         *
         * @param engine the fetch engine
         * @return this builder
         */
        public Builder engine(String engine) {
            this.engine = engine;
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
         * @return the {@code openrouter:web_fetch} server tool
         */
        public OpenRouterWebFetchServerTool build() {
            return new OpenRouterWebFetchServerTool(this);
        }
    }
}
