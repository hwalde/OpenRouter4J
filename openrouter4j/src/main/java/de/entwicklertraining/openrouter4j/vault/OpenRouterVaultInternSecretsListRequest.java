package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list one intern's secret metadata:
 * GET https://openrouter.ai/api/v1/vault/interns/{internId}/secrets
 *
 * <p>Same pagination and metadata-only semantics as the workspace list
 * ({@link OpenRouterVaultSecretsListRequest}): sorted by name, the response
 * carries the {@code data[]} page plus {@code has_more}, the plaintext is
 * never returned.
 *
 * <p>Traps: every vault route, including this list, answers 404 outside the
 * Intern API programme; regional hostnames are refused with 403.
 */
public final class OpenRouterVaultInternSecretsListRequest
        extends OpenRouterRequest<OpenRouterVaultSecretsListResponse<OpenRouterVaultInternSecretsListRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final Integer limit;
    private final Integer offset;

    private OpenRouterVaultInternSecretsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/vault/interns/")
                .append(URLEncoder.encode(internId, StandardCharsets.UTF_8))
                .append("/secrets");
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
    public OpenRouterVaultSecretsListResponse<OpenRouterVaultInternSecretsListRequest> createResponse(
            String responseBody) {
        return new OpenRouterVaultSecretsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterVaultInternSecretsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultInternSecretsListRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private Integer limit;
        private Integer offset;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
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
        public OpenRouterVaultInternSecretsListRequest build() {
            return new OpenRouterVaultInternSecretsListRequest(this);
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultInternSecretsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultInternSecretsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretsListResponse<OpenRouterVaultInternSecretsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
