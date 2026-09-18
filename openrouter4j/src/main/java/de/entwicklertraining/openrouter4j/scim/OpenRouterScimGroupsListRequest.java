package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the SCIM groups of the organization:
 * GET https://openrouter.ai/api/v1/scim/groups
 *
 * <p>The returned group ids are the {@code scim_group_id} values the
 * mapping endpoints accept.
 *
 * <p>OpenRouter requires a management key for every SCIM endpoint (see the
 * package javadoc of {@link OpenRouterScimGroupMappingsListRequest}).
 */
public final class OpenRouterScimGroupsListRequest extends OpenRouterRequest<OpenRouterScimGroupsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterScimGroupsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/scim/groups");
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
    public OpenRouterScimGroupsListResponse createResponse(String responseBody) {
        return new OpenRouterScimGroupsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupsListRequest> {

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
        public OpenRouterScimGroupsListRequest build() {
            return new OpenRouterScimGroupsListRequest(this);
        }

        @Override
        public OpenRouterScimGroupsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
