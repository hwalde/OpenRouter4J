package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A request to read the unified benchmark data:
 * GET https://openrouter.ai/api/v1/benchmarks
 *
 * <p>Aggregates scores from multiple benchmark sources (Artificial Analysis,
 * Design Arena, and OpenRouter's own tau-bench, GPQA and web-search evals).
 * The row shape depends on the requested {@code source}; the typed response
 * therefore exposes the rows as raw JSON objects plus typed {@code meta}
 * accessors. The endpoint works with any valid OpenRouter API key and is
 * rate-limited (30 requests/minute per key, 500/day per account).
 */
public final class OpenRouterBenchmarksRequest extends OpenRouterRequest<OpenRouterBenchmarksResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterBenchmarksRequest(Builder builder) {
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
        return appendQuery("/benchmarks");
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
    public OpenRouterBenchmarksResponse createResponse(String responseBody) {
        return new OpenRouterBenchmarksResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterBenchmarksRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterBenchmarksRequest> {

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
         * Sets the query key {@code source} - benchmark source to query
         * ({@code artificial-analysis}, {@code design-arena},
         * {@code openrouter}); determines the shape of the returned items.
         * When omitted, results from all sources are returned.
         *
         * @param source the benchmark source
         * @return this builder
         */
        public Builder source(String source) {
            return queryParam("source", source);
        }

        /**
         * Sets the query key {@code task_type} - filter by task type
         * ({@code coding}, {@code intelligence}, {@code agentic},
         * {@code search}); for Artificial Analysis maps to the corresponding
         * index, for Design Arena to the matching category.
         *
         * @param taskType the task type
         * @return this builder
         */
        public Builder taskType(String taskType) {
            return queryParam("task_type", taskType);
        }

        /**
         * Sets the query key {@code benchmark_type} - one exact OpenRouter
         * benchmark ({@code gpqa_diamond}, {@code tau_bench_verified_airline},
         * {@code search_browsecomp}, {@code search_hle}, {@code search_dsqa},
         * {@code search_widesearch}); a {@code search_*} value narrows the
         * response to search results only.
         *
         * @param benchmarkType the benchmark type
         * @return this builder
         */
        public Builder benchmarkType(String benchmarkType) {
            return queryParam("benchmark_type", benchmarkType);
        }

        /**
         * Sets the query key {@code include_run_config} - search benchmarks
         * only: include the published lane configuration whitelist in each
         * search item (agent turn count, reasoning effort, temperature).
         *
         * @param includeRunConfig whether to include the run config
         * @return this builder
         */
        public Builder includeRunConfig(Boolean includeRunConfig) {
            return queryParam("include_run_config", includeRunConfig);
        }

        /**
         * Sets the query key {@code search_engine} - OpenRouter search
         * benchmarks only: filter by the search engine used.
         *
         * @param searchEngine the search engine
         * @return this builder
         */
        public Builder searchEngine(String searchEngine) {
            return queryParam("search_engine", searchEngine);
        }

        /**
         * Sets the query key {@code search_surface} - OpenRouter search
         * benchmarks only: filter by the request surface the lane ran on
         * ({@code server-tool}, {@code plugin}).
         *
         * @param searchSurface the search surface
         * @return this builder
         */
        public Builder searchSurface(String searchSurface) {
            return queryParam("search_surface", searchSurface);
        }

        /**
         * Sets the query key {@code arena} - Design Arena only: arena to
         * query ({@code models}, {@code builders}, {@code agents}); defaults
         * to {@code models} when source is {@code design-arena}.
         *
         * @param arena the arena
         * @return this builder
         */
        public Builder arena(String arena) {
            return queryParam("arena", arena);
        }

        /**
         * Sets the query key {@code category} - Design Arena only: category
         * within the arena (e.g. {@code codecategories}, {@code uicomponent},
         * {@code gamedev}, {@code 3d}, {@code dataviz}, {@code image},
         * {@code video}, {@code svg}).
         *
         * @param category the category
         * @return this builder
         */
        public Builder category(String category) {
            return queryParam("category", category);
        }

        /**
         * Sets the query key {@code max_results} - maximum number of items to
         * return; when omitted, all matching results are returned.
         *
         * @param maxResults the maximum number of items
         * @return this builder
         */
        public Builder maxResults(Integer maxResults) {
            return queryParam("max_results", maxResults);
        }

        /**
         * Adds any documented query parameter verbatim, for parameters
         * OpenRouter adds later. The value is sent URL-encoded; {@code null}
         * values are ignored.
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

        @Override
        public OpenRouterBenchmarksRequest build() {
            return new OpenRouterBenchmarksRequest(this);
        }

        @Override
        public OpenRouterBenchmarksResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterBenchmarksResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterBenchmarksResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
