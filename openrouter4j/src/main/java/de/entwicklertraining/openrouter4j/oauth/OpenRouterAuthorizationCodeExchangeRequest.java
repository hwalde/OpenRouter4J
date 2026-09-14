package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to exchange an OAuth authorization code for an OpenRouter API
 * key: POST https://openrouter.ai/api/v1/auth/keys
 *
 * <p>This is the final step of the OAuth 2.0 authorization-code flow with
 * PKCE: the code is created via {@link OpenRouterCreateAuthorizationCodeRequest}
 * and delivered to the {@code callback_url} by the consent redirect, and the
 * verifier is the secret generated alongside the {@code code_challenge}. The
 * response carries the fresh API key; the existing plain-bearer path of the
 * library is untouched by this flow.
 */
public final class OpenRouterAuthorizationCodeExchangeRequest
        extends OpenRouterRequest<OpenRouterAuthorizationCodeExchangeResponse> {

    private final OpenRouterClient client;
    private final String code;
    private final String codeVerifier;
    private final String codeChallengeMethod;

    private OpenRouterAuthorizationCodeExchangeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.code = builder.code;
        this.codeVerifier = builder.codeVerifier;
        this.codeChallengeMethod = builder.codeChallengeMethod;
    }

    /**
     * @return the authorization code received from the OAuth redirect
     */
    public String code() {
        return code;
    }

    /**
     * @return the PKCE code verifier, or {@code null} when unset. Treat this
     *         value as a secret.
     */
    public String codeVerifier() {
        return codeVerifier;
    }

    /**
     * @return the PKCE code challenge method, or {@code null} when unset
     */
    public String codeChallengeMethod() {
        return codeChallengeMethod;
    }

    @Override
    public String getRelativeUrl() {
        return "/auth/keys";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code code} (required, the
     * authorization code received from the OAuth redirect),
     * {@code code_verifier} (the PKCE verifier when a challenge was used)
     * and {@code code_challenge_method} (omitted when unset; the API schema
     * also allows an explicit JSON null, but this builder simply omits the
     * field when the method was not set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("code", code);
        if (codeVerifier != null) {
            root.put("code_verifier", codeVerifier);
        }
        if (codeChallengeMethod != null) {
            root.put("code_challenge_method", codeChallengeMethod);
        }
        return root.toString();
    }

    @Override
    public OpenRouterAuthorizationCodeExchangeResponse createResponse(String responseBody) {
        return new OpenRouterAuthorizationCodeExchangeResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterAuthorizationCodeExchangeRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterAuthorizationCodeExchangeRequest> {

        private final OpenRouterClient client;
        private String code;
        private String codeVerifier;
        private String codeChallengeMethod;

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
         * Sets the required JSON field {@code code} - the authorization code
         * received from the OAuth redirect.
         *
         * @param code the authorization code
         * @return this builder
         */
        public Builder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Sets the JSON field {@code code_verifier} - the PKCE code verifier
         * if {@code code_challenge} was used in the authorization request.
         * This is the secret generated alongside the challenge; never log it.
         *
         * @param codeVerifier the PKCE code verifier
         * @return this builder
         */
        public Builder codeVerifier(String codeVerifier) {
            this.codeVerifier = codeVerifier;
            return this;
        }

        /**
         * Sets the JSON field {@code code_challenge_method} - the method
         * used to generate the code challenge: {@code "S256"} or
         * {@code "plain"}. Omit it when no method was sent on creation.
         *
         * @param codeChallengeMethod {@code "S256"} or {@code "plain"}
         * @return this builder
         */
        public Builder codeChallengeMethod(String codeChallengeMethod) {
            this.codeChallengeMethod = codeChallengeMethod;
            return this;
        }

        @Override
        public OpenRouterAuthorizationCodeExchangeRequest build() {
            if (code == null || code.isEmpty()) {
                throw new IllegalStateException("code is required to exchange an authorization code");
            }
            return new OpenRouterAuthorizationCodeExchangeRequest(this);
        }

        @Override
        public OpenRouterAuthorizationCodeExchangeResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterAuthorizationCodeExchangeResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterAuthorizationCodeExchangeResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
