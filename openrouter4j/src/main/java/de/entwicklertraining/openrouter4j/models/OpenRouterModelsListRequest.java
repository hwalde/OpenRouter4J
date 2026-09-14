package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A request to list the OpenRouter model catalog:
 * GET https://openrouter.ai/api/v1/models
 *
 * <p>The catalog carries pricing, capabilities ({@code supported_parameters}),
 * context lengths and architecture data - the fields the routing features of
 * this library depend on (see {@link OpenRouterModel#supportedParameters()}
 * for the {@code requireParameters(true)} connection).
 *
 * <p>Filters are sent as query parameters; every typed filter below mirrors a
 * documented query key of the endpoint. Filters not covered by a typed method
 * can be sent verbatim via {@link Builder#queryParam(String, String)}.
 */
public final class OpenRouterModelsListRequest extends OpenRouterRequest<OpenRouterModelsListResponse<OpenRouterModelsListRequest>> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterModelsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
    }

    /**
     * @return the query parameters this request sends, in insertion order
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        return appendQuery("/models");
    }

    private String appendQuery(String path) {
        if (queryParams.isEmpty()) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(sb.indexOf("?") < 0 ? '?' : '&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterModelsListResponse<OpenRouterModelsListRequest> createResponse(String responseBody) {
        return new OpenRouterModelsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterModelsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterModelsListRequest> {

        private final OpenRouterClient client;
        private final Map<String, String> queryParams = new LinkedHashMap<>();

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Sets the query key {@code offset} - number of records to skip for
         * pagination. When both offset and limit are omitted, the full list
         * is returned.
         *
         * @param offset number of records to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            return queryParam("offset", offset);
        }

        /**
         * Sets the query key {@code limit} - maximum number of records to
         * return (API default 500, max 1000). When both offset and limit are
         * omitted, the full list is returned.
         *
         * @param limit maximum number of records
         * @return this builder
         */
        public Builder limit(Integer limit) {
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code q} - free-text search by model name or slug.
         *
         * @param q the search text
         * @return this builder
         */
        public Builder q(String q) {
            return queryParam("q", q);
        }

        /**
         * Sets the query key {@code category} - filter by use case category
         * (e.g. {@code programming}, {@code roleplay}, {@code marketing},
         * {@code technology}, {@code science}, {@code translation}).
         *
         * @param category the category slug
         * @return this builder
         */
        public Builder category(String category) {
            return queryParam("category", category);
        }

        /**
         * Sets the query key {@code supported_parameters} - filter models by
         * supported parameter (comma-separated). Useful to pre-filter for
         * models that support {@code response_format} (structured outputs)
         * before sending a constrained request.
         *
         * @param parameters parameter names (e.g. {@code "temperature"}, {@code "response_format"})
         * @return this builder
         */
        public Builder supportedParameters(String... parameters) {
            return join("supported_parameters", parameters);
        }

        /**
         * Sets the query key {@code input_modalities} - filter by input
         * modality, comma-separated ({@code text}, {@code image}, {@code audio}, {@code file}).
         *
         * @param modalities the modalities
         * @return this builder
         */
        public Builder inputModalities(String... modalities) {
            return join("input_modalities", modalities);
        }

        /**
         * Sets the query key {@code output_modalities} - filter by output
         * modality, comma-separated ({@code text}, {@code image}, {@code audio},
         * {@code video}, {@code rerank}, ...).
         *
         * @param modalities the modalities
         * @return this builder
         */
        public Builder outputModalities(String... modalities) {
            return join("output_modalities", modalities);
        }

        /**
         * Sets the query key {@code sort} - server-side sort order
         * (e.g. {@code pricing-low-to-high}, {@code pricing-high-to-low},
         * {@code context-length-high-to-low}, {@code newest}).
         *
         * @param sort the sort key
         * @return this builder
         */
        public Builder sort(String sort) {
            return queryParam("sort", sort);
        }

        /**
         * Sets the query key {@code context} - minimum context length in
         * tokens; models with a smaller context are excluded.
         *
         * @param context the minimum context length
         * @return this builder
         */
        public Builder context(Integer context) {
            return queryParam("context", context);
        }

        /**
         * Sets the query key {@code min_price} - minimum prompt price in
         * USD per million tokens.
         *
         * @param price the minimum price
         * @return this builder
         */
        public Builder minPrice(Double price) {
            return queryParam("min_price", price);
        }

        /**
         * Sets the query key {@code max_price} - maximum prompt price in
         * USD per million tokens.
         *
         * @param price the maximum price
         * @return this builder
         */
        public Builder maxPrice(Double price) {
            return queryParam("max_price", price);
        }

        /**
         * Sets the query key {@code min_output_price} - minimum completion
         * (output) price in USD per million tokens.
         *
         * @param price the minimum price
         * @return this builder
         */
        public Builder minOutputPrice(Double price) {
            return queryParam("min_output_price", price);
        }

        /**
         * Sets the query key {@code max_output_price} - maximum completion
         * (output) price in USD per million tokens.
         *
         * @param price the maximum price
         * @return this builder
         */
        public Builder maxOutputPrice(Double price) {
            return queryParam("max_output_price", price);
        }

        /**
         * Sets the query key {@code arch} - filter by architecture/model
         * family (e.g. {@code GPT}, {@code Claude}, {@code Gemini}, {@code Llama}).
         *
         * @param arch the architecture family
         * @return this builder
         */
        public Builder arch(String arch) {
            return queryParam("arch", arch);
        }

        /**
         * Sets the query key {@code model_authors} - filter by the
         * organization that created the model (comma-separated author slugs).
         *
         * @param authors the author slugs
         * @return this builder
         */
        public Builder modelAuthors(String... authors) {
            return join("model_authors", authors);
        }

        /**
         * Sets the query key {@code providers} - filter by hosting provider
         * (comma-separated provider names).
         *
         * @param providers the provider names
         * @return this builder
         */
        public Builder providers(String... providers) {
            return join("providers", providers);
        }

        /**
         * Sets the query key {@code distillable} - {@code true} returns only
         * distillable models, {@code false} excludes them.
         *
         * @param distillable the distillation filter
         * @return this builder
         */
        public Builder distillable(Boolean distillable) {
            return queryParam("distillable", distillable);
        }

        /**
         * Sets the query key {@code zdr} - with {@code true}, only models
         * with zero-data-retention endpoints are returned.
         *
         * @param zdr the zero-data-retention filter
         * @return this builder
         */
        public Builder zdr(Boolean zdr) {
            return queryParam("zdr", zdr);
        }

        /**
         * Sets the query key {@code region} - filter to models with endpoints
         * in the given data region ({@code eu} or {@code us}).
         *
         * @param region the data region
         * @return this builder
         */
        public Builder region(String region) {
            return queryParam("region", region);
        }

        /**
         * Adds any documented query parameter verbatim, for the filters that
         * have no typed method above (the age filters {@code min_age_days} /
         * {@code max_age_days} and the Artificial Analysis index filters
         * {@code min_intelligence_index} / {@code max_intelligence_index} /
         * {@code min_coding_index} / {@code max_coding_index} /
         * {@code min_agentic_index} / {@code max_agentic_index} /
         * {@code min_tool_success_rate} / {@code max_tool_success_rate}) and
         * for any parameter OpenRouter adds later. The value is sent
         * URL-encoded; {@code null} values are ignored.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         * @return this builder
         */
        public Builder queryParam(String name, Object value) {
            if (name != null && !name.isEmpty() && value != null) {
                queryParams.put(name, String.valueOf(value));
            }
            return this;
        }

        private Builder join(String name, String... values) {
            List<String> nonNull = new ArrayList<>();
            for (String v : values) {
                if (v != null && !v.isEmpty()) {
                    nonNull.add(v);
                }
            }
            if (!nonNull.isEmpty()) {
                queryParams.put(name, String.join(",", nonNull));
            }
            return this;
        }

        @Override
        public OpenRouterModelsListRequest build() {
            return new OpenRouterModelsListRequest(this);
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterModelsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterModelsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterModelsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
