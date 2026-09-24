package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenAI {@code file_search} server tool: searches
 * uploaded vector stores and returns matching file excerpts to the model.
 * <p>
 * Emitted as {@code {"type": "file_search", "vector_store_ids": [...]}} plus
 * the explicitly configured {@code max_num_results}, {@code filters} and
 * {@code ranking_options} fields and the verbatim escape-hatch options - flat
 * fields at the top level of the tool object (this is an OpenAI-native tool
 * type, not an {@code openrouter:*} namespace tool with a {@code parameters}
 * wrapper).
 * <p>
 * {@code vector_store_ids} is required and non-empty; a request without it is
 * rejected loudly at {@code build()}.
 * <p>
 * <strong>Schema surface:</strong> the published schema declares
 * {@code FileSearchServerTool} on the Responses request's {@code tools} array
 * (and on the mid-input {@code additional_tools} item). The chat-completions
 * {@code tools} union does not list it - sending it through
 * {@code serverTools(...)} is an escape-hatch use at the caller's risk.
 * <p>
 * Traps: {@code filters} accepts a single comparison filter
 * ({@code key} / {@code type} / {@code value}) or a compound filter
 * ({@code "and"}/{@code "or"} over a filter list) - use
 * {@link Builder#filter(String, String, Object)} for the comparison form and
 * {@link Builder#filters(JSONObject)} for the compound escape hatch.
 * {@code ranking_options.ranker} is accepted verbatim (documented values
 * {@code auto} and {@code default-2024-11-15}).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">Server tools</a>
 */
public final class OpenRouterFileSearchServerTool implements OpenRouterServerTool {

    /** The tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "file_search";

    private final List<String> vectorStoreIds;
    private final Integer maxNumResults;
    private final JSONObject filters;
    private final String ranker;
    private final Double scoreThreshold;
    private final Map<String, Object> extraOptions;

    private OpenRouterFileSearchServerTool(Builder builder) {
        this.vectorStoreIds = List.copyOf(builder.vectorStoreIds);
        this.maxNumResults = builder.maxNumResults;
        this.filters = builder.filters;
        this.ranker = builder.ranker;
        this.scoreThreshold = builder.scoreThreshold;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code file_search} tool.
     *
     * @param vectorStoreIds the vector store ids to search (required, non-empty)
     * @return a new builder
     */
    public static Builder builder(List<String> vectorStoreIds) {
        return new Builder(vectorStoreIds);
    }

    /**
     * Creates a new builder for the {@code file_search} tool.
     *
     * @param vectorStoreIds the vector store ids to search (required, non-empty)
     * @return a new builder
     */
    public static Builder builder(String... vectorStoreIds) {
        return new Builder(vectorStoreIds == null ? List.of() : List.of(vectorStoreIds));
    }

    /** Returns the tool discriminator {@code "file_search"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /** Returns the required {@code vector_store_ids}, never {@code null}. */
    public List<String> vectorStoreIds() {
        return vectorStoreIds;
    }

    /** Returns the configured {@code max_num_results}, or {@code null} when unset. */
    public Integer maxNumResults() {
        return maxNumResults;
    }

    /** Returns the configured {@code filters} object, or {@code null} when unset. */
    public JSONObject filters() {
        return filters;
    }

    /** Returns the configured {@code ranking_options.ranker}, or {@code null} when unset. */
    public String ranker() {
        return ranker;
    }

    /** Returns the configured {@code ranking_options.score_threshold}, or {@code null} when unset. */
    public Double scoreThreshold() {
        return scoreThreshold;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "file_search", "vector_store_ids": [...]}} plus the
     * explicitly configured fields at the top level, followed by the verbatim
     * escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);
        JSONArray ids = new JSONArray();
        vectorStoreIds.forEach(ids::put);
        tool.put("vector_store_ids", ids);
        if (maxNumResults != null) {
            tool.put("max_num_results", maxNumResults);
        }
        if (filters != null) {
            tool.put("filters", filters);
        }
        if (ranker != null || scoreThreshold != null) {
            JSONObject ranking = new JSONObject();
            if (ranker != null) {
                ranking.put("ranker", ranker);
            }
            if (scoreThreshold != null) {
                ranking.put("score_threshold", scoreThreshold);
            }
            tool.put("ranking_options", ranking);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for {@link OpenRouterFileSearchServerTool}. Only explicitly
     * configured fields are emitted (plus the required {@code type} and
     * {@code vector_store_ids}); a verbatim {@link #option(String, Object)}
     * escape hatch covers keys this library does not type.
     */
    public static final class Builder {

        private final List<String> vectorStoreIds;
        private Integer maxNumResults;
        private JSONObject filters;
        private String ranker;
        private Double scoreThreshold;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder(List<String> vectorStoreIds) {
            Objects.requireNonNull(vectorStoreIds, "vectorStoreIds must not be null");
            if (vectorStoreIds.isEmpty()) {
                throw new IllegalArgumentException("vectorStoreIds must not be empty");
            }
            for (String id : vectorStoreIds) {
                if (id == null || id.isBlank()) {
                    throw new IllegalArgumentException("vectorStoreIds must not contain blank entries");
                }
            }
            this.vectorStoreIds = new ArrayList<>(vectorStoreIds);
        }

        /**
         * Sets {@code max_num_results}: maximum number of search results
         * returned per call. Emitted only when set.
         *
         * @param maxNumResults the maximum number of results
         * @return this builder
         */
        public Builder maxNumResults(Integer maxNumResults) {
            this.maxNumResults = maxNumResults;
            return this;
        }

        /**
         * Sets {@code filters} to a single comparison filter
         * {@code {"key": ..., "type": ..., "value": ...}}.
         * The operator is accepted verbatim; documented values are
         * {@code eq}, {@code ne}, {@code gt}, {@code gte}, {@code lt},
         * {@code lte}. The value may be a string, number, boolean or an array
         * of those. Replaces any previously set filters form.
         *
         * @param key the metadata key to compare
         * @param operator the comparison operator (required, not blank)
         * @param value the comparison value (String, Number, Boolean or List)
         * @return this builder
         */
        public Builder filter(String key, String operator, Object value) {
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(operator, "operator must not be null");
            Objects.requireNonNull(value, "value must not be null");
            if (key.isBlank()) {
                throw new IllegalArgumentException("key must not be blank");
            }
            if (operator.isBlank()) {
                throw new IllegalArgumentException("operator must not be blank");
            }
            JSONObject filter = new JSONObject();
            filter.put("key", key);
            filter.put("type", operator);
            if (value instanceof List<?> list) {
                JSONArray arr = new JSONArray();
                list.forEach(arr::put);
                filter.put("value", arr);
            } else {
                filter.put("value", value);
            }
            this.filters = filter;
            return this;
        }

        /**
         * Sets {@code filters} verbatim - escape hatch for the compound filter
         * form {@code {"type":"and"|"or","filters":[...]}} (or any future filter
         * shape). Replaces any previously set filters form.
         *
         * @param filters the filters object (may be {@code null} to unset)
         * @return this builder
         */
        public Builder filters(JSONObject filters) {
            this.filters = filters;
            return this;
        }

        /**
         * Sets {@code ranking_options.ranker}: the ranking model. Documented
         * values {@code auto} and {@code default-2024-11-15}; accepted verbatim
         * (the schema allows unknown values).
         *
         * @param ranker the ranker id (required, not blank)
         * @return this builder
         */
        public Builder ranker(String ranker) {
            Objects.requireNonNull(ranker, "ranker must not be null");
            if (ranker.isBlank()) {
                throw new IllegalArgumentException("ranker must not be blank");
            }
            this.ranker = ranker;
            return this;
        }

        /**
         * Sets {@code ranking_options.score_threshold}: minimum similarity
         * score a result must reach to be returned. Emitted only when set.
         *
         * @param scoreThreshold the minimum score
         * @return this builder
         */
        public Builder scoreThreshold(Double scoreThreshold) {
            this.scoreThreshold = scoreThreshold;
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type. Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"} or {@code "vector_store_ids"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key) || "vector_store_ids".equals(key)) {
                throw new IllegalArgumentException(
                        "The '" + key + "' field is set from the typed API and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the file-search tool.
         *
         * @return the file-search tool
         */
        public OpenRouterFileSearchServerTool build() {
            return new OpenRouterFileSearchServerTool(this);
        }
    }
}
