package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:experimental__search_models}
 * server tool: searches and filters the AI models available on OpenRouter so
 * the model can pick suitable models itself. Emitted into the {@code tools}
 * request array as {@code {"type": "openrouter:experimental__search_models"}}
 * plus a {@code parameters} object holding only the explicitly configured
 * fields.
 * <p>
 * Trap: the tool type is experimental (as the double underscore in the type
 * name signals) and may change without notice.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/search-models">Search models server tool</a>
 */
public final class OpenRouterSearchModelsServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:experimental__search_models";

    private final Integer maxResults;
    private final Map<String, Object> extraOptions;

    private OpenRouterSearchModelsServerTool(Builder builder) {
        this.maxResults = builder.maxResults;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:experimental__search_models}
     * server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:experimental__search_models"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.max_results} value (default 5,
     * max 20), or {@code null} when unset.
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:experimental__search_models"}} plus a
     * {@code parameters} object when at least one option is set.
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
     * Builder for the {@code openrouter:experimental__search_models} server
     * tool. Only explicitly configured fields are emitted; a verbatim
     * {@link #option(String, Object)} escape hatch covers configuration keys
     * this library does not know yet.
     */
    public static final class Builder {

        private Integer maxResults;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.max_results}: maximum number of models to
         * return (default 5, max 20).
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterSearchModelsServerTool}.
         */
        public OpenRouterSearchModelsServerTool build() {
            return new OpenRouterSearchModelsServerTool(this);
        }
    }
}
