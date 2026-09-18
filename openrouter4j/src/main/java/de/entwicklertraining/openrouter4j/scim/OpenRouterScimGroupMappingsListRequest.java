package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the SCIM group-to-workspace mappings:
 * GET https://openrouter.ai/api/v1/scim/group-mappings
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for every SCIM endpoint - a normal inference key is rejected with an
 * authorization error (the schema documents HTTP 401; observed rejection
 * codes vary).
 */
public final class OpenRouterScimGroupMappingsListRequest extends OpenRouterRequest<OpenRouterScimGroupMappingsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterScimGroupMappingsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/scim/group-mappings");
        String separator = "?";
        if (offset != null) {
            url.append(separator).append("offset=").append(offset);
            separator = "&";
        }
        if (limit != null) {
            url.append(separator).append("limit=").append(limit);
        }
        return url.toString();
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
    public OpenRouterScimGroupMappingsListResponse createResponse(String responseBody) {
        return new OpenRouterScimGroupMappingsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupMappingsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupMappingsListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;

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
         * Sets the {@code offset} query parameter - the number of entries to
         * skip (pagination).
         *
         * @param offset the pagination offset
         * @return this builder
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the {@code limit} query parameter - the maximum number of
         * entries to return (pagination).
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public OpenRouterScimGroupMappingsListRequest build() {
            return new OpenRouterScimGroupMappingsListRequest(this);
        }

        @Override
        public OpenRouterScimGroupMappingsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupMappingsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupMappingsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
