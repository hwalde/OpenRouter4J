package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the secrets an intern receives:
 * GET https://openrouter.ai/api/v1/vault/interns/{internId}/effective-secrets
 *
 * <p>One entry per name - the secret the intern's outbound requests
 * receive under that name: its own secrets, secrets from an attached vault
 * and workspace secrets (including ones stored before workspace-scoped
 * storage). Where several vaults hold a name, the entry is the one that
 * wins, in the order intern, attached, workspace - the same resolution
 * decides what the outbound requests receive, so this list and the
 * intern's requests agree.
 *
 * <p>Same pagination and metadata-only semantics as the vault list
 * ({@link OpenRouterVaultSecretsListRequest}): ordered by name, the
 * response carries the {@code data[]} page plus {@code has_more}, the
 * value is never returned - only {@code name}, {@code hosts},
 * {@code fingerprint}, {@code created_at} and {@code scope}.
 *
 * <p>Traps: the API answers 404 when the intern's attached vault is no
 * longer available (the intern then receives no secrets); every vault
 * route, including this read, answers 404 outside the Intern API
 * programme; regional hostnames are refused with 403. The scope is
 * selected by the API key - workspace routes act on the key's active
 * workspace and intern routes act on one intern inside that workspace,
 * there is no default workspace and no fallback to another scope. An
 * intern's own API key is confined to that intern: it can always read the
 * intern's secrets and effective secrets, and every other intern and every
 * workspace route answers 404.
 */
public final class OpenRouterVaultInternEffectiveSecretsListRequest
        extends OpenRouterRequest<OpenRouterVaultEffectiveSecretsListResponse<OpenRouterVaultInternEffectiveSecretsListRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final Integer limit;
    private final Integer offset;

    private OpenRouterVaultInternEffectiveSecretsListRequest(Builder builder) {
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

    /**
     * @return the page size, or {@code null} for the API default (100)
     */
    public Integer limit() {
        return limit;
    }

    /**
     * @return the number of entries to skip, or {@code null} for the API
     *         default (0)
     */
    public Integer offset() {
        return offset;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/vault/interns/")
                .append(URLEncoder.encode(internId, StandardCharsets.UTF_8))
                .append("/effective-secrets");
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
    public OpenRouterVaultEffectiveSecretsListResponse<OpenRouterVaultInternEffectiveSecretsListRequest> createResponse(
            String responseBody) {
        return new OpenRouterVaultEffectiveSecretsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterVaultInternEffectiveSecretsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultInternEffectiveSecretsListRequest> {

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
        public OpenRouterVaultInternEffectiveSecretsListRequest build() {
            return new OpenRouterVaultInternEffectiveSecretsListRequest(this);
        }

        @Override
        public OpenRouterVaultEffectiveSecretsListResponse<OpenRouterVaultInternEffectiveSecretsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultEffectiveSecretsListResponse<OpenRouterVaultInternEffectiveSecretsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultEffectiveSecretsListResponse<OpenRouterVaultInternEffectiveSecretsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
