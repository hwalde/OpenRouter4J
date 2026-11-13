package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to list the models the authenticated account is allowed to use:
 * GET https://openrouter.ai/api/v1/models/user
 *
 * <p>Returns the same catalog entry shape as GET /models; the response type is
 * shared with the full catalog listing.
 */
public final class OpenRouterUserModelsRequest extends OpenRouterRequest<OpenRouterModelsListResponse<OpenRouterUserModelsRequest>> {

    private final Integer offset;
    private final Integer limit;
    private final String outputModalities;
    private final String sort;
    private final String client;

    private OpenRouterUserModelsRequest(Builder builder) {
        super(builder);
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.outputModalities = builder.outputModalities;
        this.sort = builder.sort;
        this.client = builder.client;
    }

    /**
     * Returns the configured {@code offset} query parameter (number of records
     * to skip for pagination), or {@code null} when unset.
     *
     * @return the offset, or {@code null} when unset
     */
    public Integer offset() {
        return offset;
    }

    /**
     * Returns the configured {@code limit} query parameter (maximum number of
     * records to return), or {@code null} when unset.
     *
     * @return the limit, or {@code null} when unset
     */
    public Integer limit() {
        return limit;
    }

    /**
     * Returns the configured comma-separated {@code output_modalities} query
     * parameter, or {@code null} when unset.
     *
     * @return the output modalities filter, or {@code null} when unset
     */
    public String outputModalities() {
        return outputModalities;
    }

    /**
     * Returns the configured {@code sort} query parameter (server-side model
     * ordering), or {@code null} when unset (the default ordering applies).
     *
     * @return the sort value, or {@code null} when unset
     */
    public String sort() {
        return sort;
    }

    /**
     * Returns the configured {@code client} query parameter (harness
     * eligibility filter, e.g. {@code "claude_code"}, {@code "claude_desktop"},
     * {@code "codex"}), or {@code null} when unset.
     *
     * @return the client filter value, or {@code null} when unset
     */
    public String client() {
        return client;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/models/user");
        boolean first = true;
        if (offset != null) {
            sb.append("?offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (outputModalities != null) {
            sb.append(first ? '?' : '&').append("output_modalities=")
                    .append(URLEncoder.encode(outputModalities, StandardCharsets.UTF_8));
            first = false;
        }
        if (sort != null) {
            sb.append(first ? '?' : '&').append("sort=")
                    .append(URLEncoder.encode(sort, StandardCharsets.UTF_8));
            first = false;
        }
        if (client != null) {
            sb.append(first ? '?' : '&').append("client=")
                    .append(URLEncoder.encode(client, StandardCharsets.UTF_8));
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
    public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> createResponse(String responseBody) {
        return new OpenRouterModelsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterUserModelsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterUserModelsRequest> {

        private final OpenRouterClient apiClient;
        private Integer offset;
        private Integer limit;
        private String outputModalities;
        private String sort;
        private String client;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.apiClient = client;
        }

        /**
         * Sets the query key {@code offset} - number of records to skip for
         * pagination. When both offset and limit are omitted, the full list
         * is returned.
         *
         * @param offset number of records to skip, or {@code null} to unset
         * @return this builder
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the query key {@code limit} - maximum number of records to
         * return (API default 500, max 1000). When both offset and limit are
         * omitted, the full list is returned.
         *
         * @param limit maximum number of records, or {@code null} to unset
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
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
            if (modalities != null && modalities.length > 0) {
                this.outputModalities = String.join(",", modalities);
            }
            return this;
        }

        /**
         * Sets the query key {@code sort} - sort the returned models
         * server-side (prefer this over fetching the full list and sorting
         * client-side). Appended and URL-encoded only when set; passing
         * {@code null} unsets the parameter so the existing default ordering
         * is preserved.
         * <p>
         * Documented values (unknown spellings are passed through verbatim per
         * the schema's {@code x-speakeasy-unknown-values: allow}):
         * <ul>
         *   <li>{@code "most-popular"}</li>
         *   <li>{@code "newest"} (creation date)</li>
         *   <li>{@code "top-weekly"} (tokens processed in the last week)</li>
         *   <li>{@code "pricing-low-to-high"} (average prompt/completion price)</li>
         *   <li>{@code "pricing-high-to-low"} (average prompt/completion price)</li>
         *   <li>{@code "context-high-to-low"} (context length)</li>
         *   <li>{@code "throughput-high-to-low"} (recent median performance)</li>
         *   <li>{@code "latency-low-to-high"} (recent median performance)</li>
         *   <li>{@code "intelligence-high-to-low"} (Artificial Analysis index)</li>
         *   <li>{@code "coding-high-to-low"} (Artificial Analysis index)</li>
         *   <li>{@code "agentic-high-to-low"} (Artificial Analysis index)</li>
         *   <li>{@code "design-arena-elo-high-to-low"} (best Design Arena ELO across arenas)</li>
         * </ul>
         * Trap: models without a score for the chosen benchmark index are
         * placed last.
         *
         * @param sort the server-side sort key, or {@code null} to unset
         * @return this builder
         */
        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Sets the query key {@code client} - filter models and presets by the
         * named coding harness's eligibility policy. Appended and URL-encoded
         * only when set; passing {@code null} unsets the parameter so every
         * model the account can use is returned.
         * <p>
         * Documented client names (unknown spellings are passed through
         * verbatim per the schema's {@code x-speakeasy-unknown-values: allow}):
         * {@code "claude_code"}, {@code "claude_desktop"}, {@code "codex"}.
         * <p>
         * Traps:
         * <ul>
         *   <li><b>Tool-calling requirement and guardrail interaction:</b>
         *       tool calling is required for returned models, and when one of
         *       the account's guardrails sets
         *       {@code enforce_first_party_harness_models}, only the harness
         *       vendor's own models remain (OpenAI models for {@code "codex"},
         *       Anthropic models for {@code "claude_code"} and
         *       {@code "claude_desktop"}).</li>
         *   <li><b>Managed Machines policy:</b> when the workspace's Managed
         *       Machines policy sets a model list for that harness, only the
         *       listed models remain, and a preset remains only while every
         *       model it routes to is listed.</li>
         *   <li><b>Routable IDs:</b> this filter does not apply the harness's
         *       ranking, caps or Claude Desktop model ID aliases, so returned
         *       IDs stay routable.</li>
         * </ul>
         *
         * @param client the harness client identifier (e.g. {@code "claude_code"},
         *               {@code "claude_desktop"}, {@code "codex"}), or
         *               {@code null} to unset
         * @return this builder
         */
        public Builder client(String client) {
            this.client = client;
            return this;
        }

        @Override
        public OpenRouterUserModelsRequest build() {
            return new OpenRouterUserModelsRequest(this);
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> execute() {
            return apiClient.sendRequest(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> executeWithRetry() {
            return apiClient.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> executeWithExponentialBackoff() {
            return apiClient.sendRequestWithExponentialBackoff(build());
        }
    }
}
