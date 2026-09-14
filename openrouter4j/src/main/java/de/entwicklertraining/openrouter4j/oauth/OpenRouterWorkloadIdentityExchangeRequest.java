package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A request to exchange a workload identity token for a short-lived
 * OpenRouter access token:
 * POST https://openrouter.ai/api/v1/oauth/token (RFC 8693 token exchange,
 * {@code application/x-www-form-urlencoded} body - the request overrides the
 * default JSON content type accordingly).
 *
 * <p>Workload identity federation is the documented way for infrastructure
 * identities (CI, service accounts) to obtain short-lived keys without
 * embedding a long-lived key: the JWT of your identity provider is exchanged
 * for an OpenRouter access token that lives at most 15 minutes and never
 * longer than the subject token. The federation policy binds the exchange to
 * one organization (Settings -&gt; Workload identity).
 *
 * <p>The {@code grant_type} and {@code subject_token_type} form fields are
 * fixed by the API; the builder therefore has no setter for them.
 */
public final class OpenRouterWorkloadIdentityExchangeRequest
        extends OpenRouterRequest<OpenRouterWorkloadIdentityExchangeResponse> {

    /** Fixed {@code grant_type} form value mandated by the API. */
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange";

    /** Fixed {@code subject_token_type} form value mandated by the API. */
    public static final String SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt";

    /** The only scope the API offers for this exchange. */
    public static final String SCOPE_INFERENCE = "inference";

    /** The only requested token type the API accepts when one is requested. */
    public static final String REQUESTED_TOKEN_TYPE_ACCESS_TOKEN =
            "urn:ietf:params:oauth:token-type:access_token";

    private final OpenRouterClient client;
    private final String subjectToken;
    private final String federationPolicyId;
    private final String scope;
    private final String requestedTokenType;

    private OpenRouterWorkloadIdentityExchangeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.subjectToken = builder.subjectToken;
        this.federationPolicyId = builder.federationPolicyId;
        this.scope = builder.scope;
        this.requestedTokenType = builder.requestedTokenType;
    }

    /**
     * @return the identity-provider JWT to exchange. Treat this value as a
     *         credential.
     */
    public String subjectToken() {
        return subjectToken;
    }

    /**
     * @return the federation policy UUID binding the exchange to one organization
     */
    public String federationPolicyId() {
        return federationPolicyId;
    }

    /**
     * @return the requested scope, or {@code null} when unset
     */
    public String scope() {
        return scope;
    }

    /**
     * @return the requested token type, or {@code null} when unset
     */
    public String requestedTokenType() {
        return requestedTokenType;
    }

    @Override
    public String getRelativeUrl() {
        return "/oauth/token";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The endpoint wants {@code application/x-www-form-urlencoded} (RFC 8693),
     * not JSON - this override makes api-base send the matching Content-Type.
     *
     * @return {@code application/x-www-form-urlencoded}
     */
    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded";
    }

    /**
     * JSON path: none - the body is the urlencoded form: {@code grant_type}
     * (fixed constant), {@code subject_token_type} (fixed constant),
     * {@code subject_token} (required), {@code federation_policy_id}
     * (required), {@code scope} and {@code requested_token_type} (both
     * omitted when unset).
     *
     * @return the urlencoded form body of this request
     */
    @Override
    public String getBody() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", GRANT_TYPE);
        form.put("subject_token", subjectToken);
        form.put("subject_token_type", SUBJECT_TOKEN_TYPE);
        form.put("federation_policy_id", federationPolicyId);
        if (scope != null) {
            form.put("scope", scope);
        }
        if (requestedTokenType != null) {
            form.put("requested_token_type", requestedTokenType);
        }
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            body.append('=');
            body.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return body.toString();
    }

    @Override
    public OpenRouterWorkloadIdentityExchangeResponse createResponse(String responseBody) {
        return new OpenRouterWorkloadIdentityExchangeResponse(
                new org.json.JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterWorkloadIdentityExchangeRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterWorkloadIdentityExchangeRequest> {

        private final OpenRouterClient client;
        private String subjectToken;
        private String federationPolicyId;
        private String scope;
        private String requestedTokenType;

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
         * Sets the {@code subject_token} form field - the JWT issued by your
         * identity provider (max 16384 characters, three dot-separated
         * segments). Treat this value as a credential.
         *
         * @param subjectToken the identity-provider JWT
         * @return this builder
         */
        public Builder subjectToken(String subjectToken) {
            this.subjectToken = subjectToken;
            return this;
        }

        /**
         * Sets the {@code federation_policy_id} form field - the federation
         * policy to evaluate (from Settings -&gt; Workload identity); it
         * binds the exchange to one organization.
         *
         * @param federationPolicyId the federation policy UUID
         * @return this builder
         */
        public Builder federationPolicyId(String federationPolicyId) {
            this.federationPolicyId = federationPolicyId;
            return this;
        }

        /**
         * Sets the {@code scope} form field - only {@code inference} is
         * available (see {@link #SCOPE_INFERENCE}). Omitted when unset.
         *
         * @param scope the requested scope, currently only {@code inference}
         * @return this builder
         */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets the {@code requested_token_type} form field - when present it
         * must be {@link #REQUESTED_TOKEN_TYPE_ACCESS_TOKEN}. Omitted when
         * unset.
         *
         * @param requestedTokenType the requested token type
         * @return this builder
         */
        public Builder requestedTokenType(String requestedTokenType) {
            this.requestedTokenType = requestedTokenType;
            return this;
        }

        @Override
        public OpenRouterWorkloadIdentityExchangeRequest build() {
            if (subjectToken == null || subjectToken.isEmpty()) {
                throw new IllegalStateException("subjectToken is required for a workload identity exchange");
            }
            if (federationPolicyId == null || federationPolicyId.isEmpty()) {
                throw new IllegalStateException(
                        "federationPolicyId is required for a workload identity exchange");
            }
            return new OpenRouterWorkloadIdentityExchangeRequest(this);
        }

        @Override
        public OpenRouterWorkloadIdentityExchangeResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterWorkloadIdentityExchangeResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterWorkloadIdentityExchangeResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
