package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read one budget guardrail policy:
 * 
 * GET https://openrouter.ai/api/v1/guardrails/policies/budget/{id}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailBudgetPolicyGetRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyDetailResponse<OpenRouterGuardrailBudgetPolicyGetRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final String workspaceId;

    private OpenRouterGuardrailBudgetPolicyGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.workspaceId = builder.workspaceId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/policies/budget/")
                .append(URLEncoder.encode(id, StandardCharsets.UTF_8));
        boolean first = true;
        if (workspaceId != null) {
            sb.append(first ? '?' : '&').append("workspace_id=")
                    .append(URLEncoder.encode(workspaceId, StandardCharsets.UTF_8));
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
    public OpenRouterGuardrailPolicyDetailResponse<OpenRouterGuardrailBudgetPolicyGetRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyDetailResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailBudgetPolicyGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailBudgetPolicyGetRequest> {

        private final OpenRouterClient client;
        private String id;
        private String workspaceId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the policy
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
        this.id = id;
        }


    /**
     * * Sets the query parameter {@code workspace_id} (required): the
     *          * workspace the policy belongs to. Validated loudly at
     *          * {@link #build()}.
     */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterGuardrailBudgetPolicyGetRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if (".".equals(id) || "..".equals(id)) {
            throw new IllegalStateException("id of \".\" or \"..\" cannot be addressed by path");
        }
            return new OpenRouterGuardrailBudgetPolicyGetRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyDetailResponse<OpenRouterGuardrailBudgetPolicyGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyDetailResponse<OpenRouterGuardrailBudgetPolicyGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyDetailResponse<OpenRouterGuardrailBudgetPolicyGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
