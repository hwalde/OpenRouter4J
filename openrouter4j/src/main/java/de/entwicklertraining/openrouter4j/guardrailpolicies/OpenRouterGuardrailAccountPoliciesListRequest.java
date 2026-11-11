package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the workspace-wide (account-level) guardrail policies of the
 * account: 
 * GET https://openrouter.ai/api/v1/guardrails/policies/account
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailAccountPoliciesListRequest extends OpenRouterRequest<OpenRouterGuardrailAccountPoliciesListResponse<OpenRouterGuardrailAccountPoliciesListRequest>> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterGuardrailAccountPoliciesListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/policies/account");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset.intValue());
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit.intValue());
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * This request carries no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }
    @Override
    public OpenRouterGuardrailAccountPoliciesListResponse<OpenRouterGuardrailAccountPoliciesListRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailAccountPoliciesListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailAccountPoliciesListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailAccountPoliciesListRequest> {

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
     * * Sets the query parameter {@code offset}: number of records to skip
     *          * for pagination. When both {@code offset} and {@code limit} are
     *          * omitted, the full list is returned. {@code null} unsets the
     *          * parameter.
     */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

    /**
     * * Sets the query parameter {@code limit}: maximum number of records
     *          * to return (1-100). When both {@code offset} and {@code limit} are
     *          * omitted, the full list is returned. {@code null} unsets the
     *          * parameter.
     */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public OpenRouterGuardrailAccountPoliciesListRequest build() {
        if (limit != null && (limit.intValue() < 1 || limit.intValue() > 100)) {
            throw new IllegalStateException("limit must be between 1 and 100");
        }
        if (offset != null && offset.intValue() < 0) {
            throw new IllegalStateException("offset must be at least 0");
        }
            return new OpenRouterGuardrailAccountPoliciesListRequest(this);
        }

        @Override
        public OpenRouterGuardrailAccountPoliciesListResponse<OpenRouterGuardrailAccountPoliciesListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailAccountPoliciesListResponse<OpenRouterGuardrailAccountPoliciesListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailAccountPoliciesListResponse<OpenRouterGuardrailAccountPoliciesListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
