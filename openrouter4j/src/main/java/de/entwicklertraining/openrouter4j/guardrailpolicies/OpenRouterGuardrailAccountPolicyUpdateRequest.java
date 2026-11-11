package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 * A request to update one workspace-wide (account-level) guardrail policy:
 * 
 * PUT https://openrouter.ai/api/v1/guardrails/policies/account/{id}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>The body is one {@link OpenRouterGuardrailPolicyRequest} - the
 * discriminated {@code budget} / {@code model_availability} /
 * {@code moderation} union - carried verbatim.
 */
public final class OpenRouterGuardrailAccountPolicyUpdateRequest extends OpenRouterRequest<OpenRouterGuardrailAccountPolicyResponse<OpenRouterGuardrailAccountPolicyUpdateRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final OpenRouterGuardrailPolicyRequest policy;

    private OpenRouterGuardrailAccountPolicyUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.policy = builder.policy;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /** @return the policy request value as sent */
    public OpenRouterGuardrailPolicyRequest policy() {
        return policy;
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/policies/account/" +
                URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PUT";
    }

    @Override
    public String getBody() {
        return policy.toJson().toString();
    }

    @Override
    public OpenRouterGuardrailAccountPolicyResponse<OpenRouterGuardrailAccountPolicyUpdateRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailAccountPolicyResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailAccountPolicyUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailAccountPolicyUpdateRequest> {

        private final OpenRouterClient client;
        private final String id;
        private OpenRouterGuardrailPolicyRequest policy;

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
         * Sets the request body: one workspace-wide (account-level) policy
         * ({@link OpenRouterGuardrailPolicyRequest} - its
         * {@code budget} / {@code model_availability} /
         * {@code moderation} factories or the raw hatch). Required.
         */
        public Builder policy(OpenRouterGuardrailPolicyRequest policy) {
            this.policy = policy;
            return this;
        }

        @Override
        public OpenRouterGuardrailAccountPolicyUpdateRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required");
            }
            if (".".equals(id) || "..".equals(id)) {
                throw new IllegalStateException("id of \".\" or \"..\" cannot be addressed by path");
            }
            if (policy == null) {
                throw new IllegalStateException("policy is required");
            }
            return new OpenRouterGuardrailAccountPolicyUpdateRequest(this);
        }

        @Override
        public OpenRouterGuardrailAccountPolicyResponse<OpenRouterGuardrailAccountPolicyUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailAccountPolicyResponse<OpenRouterGuardrailAccountPolicyUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailAccountPolicyResponse<OpenRouterGuardrailAccountPolicyUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
