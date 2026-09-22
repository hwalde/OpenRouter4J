package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the workspace secret metadata:
 * GET https://openrouter.ai/api/v1/vault/secrets
 *
 * <p>Scope is selected by the API key's active workspace - there is no
 * workspace parameter and no fallback. Sorted by name; the response carries
 * the {@code data[]} page plus {@code has_more}.
 *
 * <p>Traps: the rows are metadata only (the plaintext is never returned);
 * every vault route, including this list, answers 404 outside the Intern API
 * programme.
 *
 * <p>See the <a href="https://openrouter.ai/docs/guides/ori/vault">vault docs</a>.
 */
public final class OpenRouterVaultSecretsListRequest
        extends OpenRouterRequest<OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest>> {

    private final OpenRouterClient client;
    private final Integer limit;
    private final Integer offset;

    private OpenRouterVaultSecretsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/vault/secrets");
        boolean first = true;
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
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
    public OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> createResponse(
            String responseBody) {
        return new OpenRouterVaultSecretsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVaultSecretsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultSecretsListRequest> {

        private final OpenRouterClient client;
        private Integer limit;
        private Integer offset;

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
         * Sets the query key {@code limit} - the page size. The API default
         * is 100; accepted range 1-100 (validated loudly).
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
         * Sets the query key {@code offset} - the number of entries to skip.
         * The API default is 0; accepted range 0-10,000 (validated loudly).
         *
         * @param offset the number of entries to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            if (offset != null && (offset < 0 || offset > 10_000)) {
                throw new IllegalArgumentException("offset must be between 0 and 10000");
            }
            this.offset = offset;
            return this;
        }

        @Override
        public OpenRouterVaultSecretsListRequest build() {
            return new OpenRouterVaultSecretsListRequest(this);
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
