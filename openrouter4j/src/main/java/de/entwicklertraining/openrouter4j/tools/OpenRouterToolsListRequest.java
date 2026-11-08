package de.entwicklertraining.openrouter4j.tools;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read the server-tool catalog:
 * GET https://openrouter.ai/api/v1/tools
 *
 * <p>Lists every server tool OpenRouter can run on behalf of a model:
 * accepted {@code tools[].type} spellings per API format, the engines behind
 * it with their pricing, and how many endpoints run it natively. The
 * discovery counterpart for the request-side {@code OpenRouterServerTool}
 * family - validate a {@code tools[].type} spelling against
 * {@link OpenRouterServerToolCatalogEntry#aliases()} and reason about tool
 * billing against {@link OpenRouterServerToolEngine#pricingSource()}.
 *
 * <p>Pagination is offset-based; when both {@code offset} and
 * {@code limit} are omitted the API returns the full list in one page.
 */
public final class OpenRouterToolsListRequest
        extends OpenRouterRequest<OpenRouterToolsListResponse<OpenRouterToolsListRequest>> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String apiFormat;

    private OpenRouterToolsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.apiFormat = builder.apiFormat;
    }

    /**
     * @return the number of records to skip, or {@code null} for the API
     *         default (0)
     */
    public Integer offset() {
        return offset;
    }

    /**
     * @return the page size, or {@code null} for the API default (50)
     */
    public Integer limit() {
        return limit;
    }

    /**
     * @return the API-format filter, or {@code null} when unset
     */
    public String apiFormat() {
        return apiFormat;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/tools");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (apiFormat != null) {
            sb.append(first ? '?' : '&').append("api_format=")
                    .append(URLEncoder.encode(apiFormat, StandardCharsets.UTF_8));
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
    public OpenRouterToolsListResponse<OpenRouterToolsListRequest> createResponse(String responseBody) {
        return new OpenRouterToolsListResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterToolsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterToolsListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;
        private String apiFormat;

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
         * Sets the query key {@code offset} - the number of records to skip.
         * The API default is 0; the minimum is 0 (validated loudly).
         * {@code null} unsets the parameter.
         *
         * @param offset the number of records to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            if (offset != null && offset < 0) {
                throw new IllegalArgumentException("offset must be at least 0");
            }
            this.offset = offset;
            return this;
        }

        /**
         * Sets the query key {@code limit} - the page size. The API default
         * is 50; accepted range 1-100 (validated loudly). {@code null}
         * unsets the parameter.
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 100)) {
                throw new IllegalArgumentException("limit must be between 1 and 100");
            }
            this.limit = limit;
            return this;
        }

        /**
         * Sets the query key {@code api_format} - only tools usable on this
         * API format are returned. The documented values are
         * {@code responses}, {@code chat-completions} and
         * {@code anthropic-messages}; unknown spellings are passed through
         * verbatim. A blank value is rejected loudly because it names no
         * API format. {@code null} unsets the parameter.
         *
         * @param apiFormat the API format to filter by
         * @return this builder
         */
        public Builder apiFormat(String apiFormat) {
            if (apiFormat != null && apiFormat.isBlank()) {
                throw new IllegalArgumentException("apiFormat must not be blank");
            }
            this.apiFormat = apiFormat;
            return this;
        }

        @Override
        public OpenRouterToolsListRequest build() {
            return new OpenRouterToolsListRequest(this);
        }

        @Override
        public OpenRouterToolsListResponse<OpenRouterToolsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterToolsListResponse<OpenRouterToolsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterToolsListResponse<OpenRouterToolsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}