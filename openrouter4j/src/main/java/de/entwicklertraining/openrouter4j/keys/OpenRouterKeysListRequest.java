package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to list the API keys of the account:
 * GET https://openrouter.ai/api/v1/keys
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected (observed live
 * 2026-09-16 as HTTP 401 "Invalid API key"; the schema documents 403).
 * By default only keys of the default workspace are returned.
 */
public final class OpenRouterKeysListRequest extends OpenRouterRequest<OpenRouterKeysListResponse> {

    private final OpenRouterClient client;
    private final Boolean includeDisabled;
    private final Integer offset;
    private final String workspaceId;

    private OpenRouterKeysListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.includeDisabled = builder.includeDisabled;
        this.offset = builder.offset;
        this.workspaceId = builder.workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/keys");
        boolean first = true;
        if (includeDisabled != null) {
            sb.append("?include_disabled=").append(includeDisabled);
            first = false;
        }
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (workspaceId != null && !workspaceId.isEmpty()) {
            sb.append(first ? '?' : '&').append("workspace_id=")
                    .append(URLEncoder.encode(workspaceId, StandardCharsets.UTF_8));
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
    public OpenRouterKeysListResponse createResponse(String responseBody) {
        return new OpenRouterKeysListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterKeysListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterKeysListRequest> {

        private final OpenRouterClient client;
        private Boolean includeDisabled;
        private Integer offset;
        private String workspaceId;

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
         * Sets the query key {@code include_disabled} - whether to include
         * disabled API keys in the response.
         *
         * @param includeDisabled whether to include disabled keys
         * @return this builder
         */
        public Builder includeDisabled(Boolean includeDisabled) {
            this.includeDisabled = includeDisabled;
            return this;
        }

        /**
         * Sets the query key {@code offset} - number of API keys to skip for
         * pagination.
         *
         * @param offset the number of keys to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the query key {@code workspace_id} - filter API keys by
         * workspace. By default, keys in the default workspace are returned.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterKeysListRequest build() {
            return new OpenRouterKeysListRequest(this);
        }

        @Override
        public OpenRouterKeysListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterKeysListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterKeysListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
