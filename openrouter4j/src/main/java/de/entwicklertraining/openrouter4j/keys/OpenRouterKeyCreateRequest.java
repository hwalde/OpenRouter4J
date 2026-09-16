package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to create a new API key:
 * POST https://openrouter.ai/api/v1/keys
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected (observed live
 * 2026-09-16 as HTTP 401 "Invalid API key"; the schema documents 403).
 *
 * <p>Secrets rule: the response carries the plaintext key
 * (see {@link OpenRouterKeyCreateResponse#key()}) exactly once - it cannot be
 * retrieved later. Do not log it. The optional {@code external.api_key}
 * builder value is also a secret: it is stored as a SHA-256 hash server-side
 * and never returned.
 */
public final class OpenRouterKeyCreateRequest extends OpenRouterRequest<OpenRouterKeyCreateResponse> {

    private final OpenRouterClient client;
    private final String name;
    private final Double limit;
    private final String limitReset;
    private final String expiresAt;
    private final Boolean includeByokInLimit;
    private final String creatorUserId;
    private final String workspaceId;
    private final String externalUser;
    private final String externalApiKey;

    private OpenRouterKeyCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
        this.limit = builder.limit;
        this.limitReset = builder.limitReset;
        this.expiresAt = builder.expiresAt;
        this.includeByokInLimit = builder.includeByokInLimit;
        this.creatorUserId = builder.creatorUserId;
        this.workspaceId = builder.workspaceId;
        this.externalUser = builder.externalUser;
        this.externalApiKey = builder.externalApiKey;
    }

    /**
     * @return the configured key name
     */
    public String name() {
        return name;
    }

    @Override
    public String getRelativeUrl() {
        return "/keys";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (limit != null) {
            body.put("limit", limit);
        }
        if (limitReset != null) {
            body.put("limit_reset", limitReset);
        }
        if (expiresAt != null) {
            body.put("expires_at", expiresAt);
        }
        if (includeByokInLimit != null) {
            body.put("include_byok_in_limit", includeByokInLimit);
        }
        if (creatorUserId != null) {
            body.put("creator_user_id", creatorUserId);
        }
        if (workspaceId != null) {
            body.put("workspace_id", workspaceId);
        }
        if (externalUser != null || externalApiKey != null) {
            JSONObject external = new JSONObject();
            if (externalUser != null) {
                external.put("user", externalUser);
            }
            if (externalApiKey != null) {
                external.put("api_key", externalApiKey);
            }
            body.put("external", external);
        }
        return body.toString();
    }

    @Override
    public OpenRouterKeyCreateResponse createResponse(String responseBody) {
        return new OpenRouterKeyCreateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterKeyCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterKeyCreateRequest> {

        private final OpenRouterClient client;
        private String name;
        private Double limit;
        private String limitReset;
        private String expiresAt;
        private Boolean includeByokInLimit;
        private String creatorUserId;
        private String workspaceId;
        private String externalUser;
        private String externalApiKey;

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
         * Sets the body field {@code name} (required) - the name of the new
         * API key.
         *
         * @param name the key name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the body field {@code limit} (optional) - spending limit in
         * USD.
         *
         * @param limit the spending limit
         * @return this builder
         */
        public Builder limit(Double limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Sets the body field {@code limit_reset} (optional) - reset type
         * ({@code daily}, {@code weekly}, {@code monthly}); resets happen
         * automatically at midnight UTC, weeks are Monday through Sunday.
         *
         * @param limitReset the reset type
         * @return this builder
         */
        public Builder limitReset(String limitReset) {
            this.limitReset = limitReset;
            return this;
        }

        /**
         * Sets the body field {@code expires_at} (optional) - ISO 8601 UTC
         * expiration timestamp. Trap: must include seconds
         * ({@code YYYY-MM-DDTHH:MM:SSZ}); minute-precision timestamps are
         * rejected.
         *
         * @param expiresAt the expiration timestamp
         * @return this builder
         */
        public Builder expiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * Sets the body field {@code include_byok_in_limit} (optional) -
         * whether BYOK usage counts into the limit.
         *
         * @param includeByokInLimit whether to include BYOK usage
         * @return this builder
         */
        public Builder includeByokInLimit(Boolean includeByokInLimit) {
            this.includeByokInLimit = includeByokInLimit;
            return this;
        }

        /**
         * Sets the body field {@code creator_user_id} (optional) - user ID of
         * the key creator; only meaningful for organization-owned keys where
         * a specific member creates the key.
         *
         * @param creatorUserId the creator user ID
         * @return this builder
         */
        public Builder creatorUserId(String creatorUserId) {
            this.creatorUserId = creatorUserId;
            return this;
        }

        /**
         * Sets the body field {@code workspace_id} (optional) - the workspace
         * to create the key in; defaults to the default workspace.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the body field {@code external.user} (optional) - the
         * partner's end-user identifier for attribution.
         *
         * @param externalUser the partner's end-user identifier
         * @return this builder
         */
        public Builder externalUser(String externalUser) {
            this.externalUser = externalUser;
            return this;
        }

        /**
         * Sets the body field {@code external.api_key} (optional) - a
         * partner-supplied API key (minimum 32 characters, sufficient
         * entropy). Stored as a SHA-256 hash and never returned - treat this
         * value as a secret and do not log it.
         *
         * @param externalApiKey the partner's API key
         * @return this builder
         */
        public Builder externalApiKey(String externalApiKey) {
            this.externalApiKey = externalApiKey;
            return this;
        }

        @Override
        public OpenRouterKeyCreateRequest build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("name is required for key creation");
            }
            return new OpenRouterKeyCreateRequest(this);
        }

        @Override
        public OpenRouterKeyCreateResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterKeyCreateResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterKeyCreateResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
