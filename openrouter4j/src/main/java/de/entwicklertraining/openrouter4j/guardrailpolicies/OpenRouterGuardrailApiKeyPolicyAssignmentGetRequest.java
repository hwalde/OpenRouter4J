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
 * A request to read the guardrail-policy assignment of one API key:
 * 
 * GET https://openrouter.ai/api/v1/guardrails/policies/assignments/api-keys/{hash}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest>> {

    private final OpenRouterClient client;
    private final String hash;
    private final String workspaceId;
    private final String policyType;

    private OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.hash = builder.hash;
        this.workspaceId = builder.workspaceId;
        this.policyType = builder.policyType;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String hash() {
        return URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/policies/assignments/api-keys/")
                .append(URLEncoder.encode(hash, StandardCharsets.UTF_8));
        boolean first = true;
        if (workspaceId != null) {
            sb.append(first ? '?' : '&').append("workspace_id=")
                    .append(URLEncoder.encode(workspaceId, StandardCharsets.UTF_8));
            first = false;
        }
        if (policyType != null) {
            sb.append(first ? '?' : '&').append("policy_type=")
                    .append(URLEncoder.encode(policyType, StandardCharsets.UTF_8));
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
    public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyAssignmentResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest> {

        private final OpenRouterClient client;
        private String hash;
        private String workspaceId;
        private String policyType;
        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param hash api keys the subject's identifying path segment
         */
        public Builder(OpenRouterClient client, String hash) {
            super(client);
            this.client = client;
            this.hash = hash;
        }

        /**
         * Sets the query parameter {@code workspace_id} (required): the
         * workspace the assignment is read for.
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the query parameter {@code policy_type} (required): the
         * policy type the assignment is read for - the documented values are
         * {@code "budget"}, {@code "model_availability"}, {@code "moderation"}
         * and {@code "rate_limit"} (undocumented in CRUD - it appears in the
         * assignment enum but has no policy CRUD routes of its own); unknown
         * spellings are passed through verbatim.
         */
        public Builder policyType(String policyType) {
            this.policyType = policyType;
            return this;
        }

        @Override
        public OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest build() {
            if (hash == null || hash.isEmpty()) {
                throw new IllegalStateException("hash is required");
            }
            if (".".equals(hash) || "..".equals(hash)) {
                throw new IllegalStateException("hash of \".\" or \"..\" cannot be addressed by path");
            }
            if (workspaceId == null || workspaceId.isEmpty()) {
                throw new IllegalStateException("workspace_id is required");
            }
            if (policyType == null || policyType.isEmpty()) {
                throw new IllegalStateException("policy_type is required");
            }
            return new OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
