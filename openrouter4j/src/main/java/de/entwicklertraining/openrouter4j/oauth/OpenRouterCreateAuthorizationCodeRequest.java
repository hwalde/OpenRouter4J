package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to create an OAuth authorization code:
 * POST https://openrouter.ai/api/v1/auth/keys/code
 *
 * <p>This is step one of the OAuth 2.0 authorization-code flow with PKCE
 * that turns a user consent into an OpenRouter API key: the user visits the
 * consent page, OpenRouter redirects to the {@code callback_url} with the
 * authorization code, and the code (plus the PKCE verifier) is exchanged via
 * {@link OpenRouterAuthorizationCodeExchangeRequest}. The existing
 * plain-bearer path of the library is untouched by this flow.
 *
 * <p>Only {@code callback_url} is required. Pair the optional PKCE fields
 * with {@link OpenRouterPkce#generateCodeVerifier()} and
 * {@link OpenRouterPkce#codeChallengeS256(String)}.
 */
public final class OpenRouterCreateAuthorizationCodeRequest
        extends OpenRouterRequest<OpenRouterCreateAuthorizationCodeResponse> {

    private final OpenRouterClient client;
    private final String callbackUrl;
    private final String codeChallenge;
    private final String codeChallengeMethod;
    private final String expiresAt;
    private final String keyLabel;
    private final Double limit;
    private final String usageLimitType;
    private final String workspaceId;

    private OpenRouterCreateAuthorizationCodeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.callbackUrl = builder.callbackUrl;
        this.codeChallenge = builder.codeChallenge;
        this.codeChallengeMethod = builder.codeChallengeMethod;
        this.expiresAt = builder.expiresAt;
        this.keyLabel = builder.keyLabel;
        this.limit = builder.limit;
        this.usageLimitType = builder.usageLimitType;
        this.workspaceId = builder.workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        return "/auth/keys/code";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code callback_url} (required),
     * {@code code_challenge}, {@code code_challenge_method},
     * {@code expires_at}, {@code key_label}, {@code limit},
     * {@code usage_limit_type} and {@code workspace_id} (all omitted when
     * unset).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("callback_url", callbackUrl);
        if (codeChallenge != null) {
            root.put("code_challenge", codeChallenge);
        }
        if (codeChallengeMethod != null) {
            root.put("code_challenge_method", codeChallengeMethod);
        }
        if (expiresAt != null) {
            root.put("expires_at", expiresAt);
        }
        if (keyLabel != null) {
            root.put("key_label", keyLabel);
        }
        if (limit != null) {
            root.put("limit", limit);
        }
        if (usageLimitType != null) {
            root.put("usage_limit_type", usageLimitType);
        }
        if (workspaceId != null) {
            root.put("workspace_id", workspaceId);
        }
        return root.toString();
    }

    @Override
    public OpenRouterCreateAuthorizationCodeResponse createResponse(String responseBody) {
        return new OpenRouterCreateAuthorizationCodeResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterCreateAuthorizationCodeRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterCreateAuthorizationCodeRequest> {

        private final OpenRouterClient client;
        private String callbackUrl;
        private String codeChallenge;
        private String codeChallengeMethod;
        private String expiresAt;
        private String keyLabel;
        private Double limit;
        private String usageLimitType;
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
         * Sets the required JSON field {@code callback_url} - the URL
         * OpenRouter redirects to after authorization. Supports https URLs
         * and localhost/127.0.0.1 URLs on any port for local CLI tools.
         *
         * @param callbackUrl the redirect target of the consent flow
         * @return this builder
         */
        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        /**
         * Sets the JSON field {@code code_challenge} - the PKCE code
         * challenge. Derive it with
         * {@link OpenRouterPkce#codeChallengeS256(String)} and keep the
         * verifier secret until the exchange.
         *
         * @param codeChallenge the PKCE code challenge
         * @return this builder
         */
        public Builder codeChallenge(String codeChallenge) {
            this.codeChallenge = codeChallenge;
            return this;
        }

        /**
         * Sets the JSON field {@code code_challenge_method} - the method
         * used to generate the code challenge: {@code "S256"} (recommended)
         * or {@code "plain"}. Must match how {@link #codeChallenge(String)}
         * was derived.
         *
         * @param codeChallengeMethod {@code "S256"} or {@code "plain"}
         * @return this builder
         */
        public Builder codeChallengeMethod(String codeChallengeMethod) {
            this.codeChallengeMethod = codeChallengeMethod;
            return this;
        }

        /**
         * Sets the JSON field {@code expires_at} - an optional ISO 8601 UTC
         * expiration timestamp for the code. Trap: the API requires seconds
         * precision ({@code YYYY-MM-DDTHH:MM:SSZ}; fractional seconds
         * allowed) and rejects minute-precision timestamps.
         *
         * @param expiresAt the expiration timestamp, seconds precision
         * @return this builder
         */
        public Builder expiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * Sets the JSON field {@code key_label} - an optional custom label
         * for the API key created by the exchange (max 100 characters).
         * Defaults to the app name when unset.
         *
         * @param keyLabel the label of the future API key
         * @return this builder
         */
        public Builder keyLabel(String keyLabel) {
            this.keyLabel = keyLabel;
            return this;
        }

        /**
         * Sets the JSON field {@code limit} - the credit limit for the API
         * key created by the exchange.
         *
         * @param limit the credit limit in credits
         * @return this builder
         */
        public Builder limit(Double limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Sets the JSON field {@code usage_limit_type} - the reset interval
         * of the credit limit: {@code "daily"}, {@code "weekly"} or
         * {@code "monthly"}. Only meaningful together with
         * {@link #limit(Double)}.
         *
         * @param usageLimitType {@code "daily"}, {@code "weekly"} or {@code "monthly"}
         * @return this builder
         */
        public Builder usageLimitType(String usageLimitType) {
            this.usageLimitType = usageLimitType;
            return this;
        }

        /**
         * Sets the JSON field {@code workspace_id} - an optional workspace
         * ID to associate the created API key with.
         *
         * @param workspaceId the workspace UUID
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterCreateAuthorizationCodeRequest build() {
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                throw new IllegalStateException("callbackUrl is required to create an authorization code");
            }
            return new OpenRouterCreateAuthorizationCodeRequest(this);
        }

        @Override
        public OpenRouterCreateAuthorizationCodeResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterCreateAuthorizationCodeResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterCreateAuthorizationCodeResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
