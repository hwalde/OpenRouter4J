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
 * A request to change the guardrail-policy assignment of one API key:
 * 
 * PUT https://openrouter.ai/api/v1/guardrails/policies/assignments/api-keys/{hash}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest>> {

    private final OpenRouterClient client;
    private final String hash;
    private final String workspaceId;
    private final String policyType;
    private final String policyId;
    private final Boolean assigned;
    private final OpenRouterGuardrailPolicyAssignmentState expectedAssignment;

    private OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.hash = builder.hash;
        this.workspaceId = builder.workspaceId;
        this.policyType = builder.policyType;
        this.policyId = builder.policyId;
        this.assigned = builder.assigned;
        this.expectedAssignment = builder.expectedAssignment;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String hash() {
        return URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/policies/assignments/api-keys/" +
                URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PUT";
    }

    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("workspace_id", workspaceId);
        body.put("policy_type", policyType);
        body.put("policy_id", policyId);
        body.put("assigned", assigned.booleanValue());
        body.put("expected_assignment", expectedAssignment.toJson());
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyAssignmentChangeResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest> {

        private final OpenRouterClient client;
        private String hash;
        private String workspaceId;
        private String policyType;
        private String policyId;
        private Boolean assigned;
        private OpenRouterGuardrailPolicyAssignmentState expectedAssignment;
        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param hash the API key hash identifying the subject (URL-encoded into the path segment)
         */
        public Builder(OpenRouterClient client, String hash) {
            super(client);
            this.client = client;
            this.hash = hash;
        }

        /**
         * Sets the body field {@code workspace_id} (required): the workspace
         * the policy belongs to.
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the body field {@code policy_type} (required): the policy
         * type the assignment is changed for - the documented values are
         * {@code "budget"}, {@code "model_availability"}, {@code "moderation"}
         * and {@code "rate_limit"} (undocumented in CRUD - it appears in the
         * assignment enum but has no policy CRUD routes of its own); unknown
         * spellings are passed through verbatim.
         */
        public Builder policyType(String policyType) {
            this.policyType = policyType;
            return this;
        }

        /**
         * Sets the body field {@code policy_id} (required): the policy to
         * assign or remove.
         */
        public Builder policyId(String policyId) {
            this.policyId = policyId;
            return this;
        }

        /**
         * Sets the body field {@code assigned} (required): {@code true}
         * assigns the policy to the subject, {@code false} removes it.
         */
        public Builder assigned(Boolean assigned) {
            this.assigned = assigned;
            return this;
        }

        /**
         * Sets the body field {@code expected_assignment} (required): the
         * assignment state last read for the subject
         * ({@link OpenRouterGuardrailPolicyAssignmentState} - build it with
         * {@link OpenRouterGuardrailPolicyAssignmentState#of(List, List,
         * boolean)} from the read response or pass the response's
         * {@code data()} verbatim). The request fails with HTTP 409 when the
         * stored state changed since it was read (optimistic concurrency) -
         * re-read the assignment and retry with the new state.
         */
        public Builder expectedAssignment(OpenRouterGuardrailPolicyAssignmentState expectedAssignment) {
            this.expectedAssignment = expectedAssignment;
            return this;
        }

        @Override
        public OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest build() {
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
            if (policyId == null || policyId.isEmpty()) {
                throw new IllegalStateException("policy_id is required");
            }
            if (assigned == null) {
                throw new IllegalStateException("assigned is required");
            }
            if (expectedAssignment == null) {
                throw new IllegalStateException("expected_assignment is required");
            }
            return new OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
