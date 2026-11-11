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
 * A request to change the guardrail-policy assignment of one organization member:
 * 
 * PUT https://openrouter.ai/api/v1/guardrails/policies/assignments/members/{member_id}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailMemberPolicyAssignmentChangeRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailMemberPolicyAssignmentChangeRequest>> {

    private final OpenRouterClient client;
    private final String memberId;
    private final String workspaceId;
    private final String policyType;
    private final String policyId;
    private final Boolean assigned;
    private final OpenRouterGuardrailPolicyAssignmentState expectedAssignment;

    private OpenRouterGuardrailMemberPolicyAssignmentChangeRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.memberId = builder.memberId;
        this.workspaceId = builder.workspaceId;
        this.policyType = builder.policyType;
        this.policyId = builder.policyId;
        this.assigned = builder.assigned;
        this.expectedAssignment = builder.expectedAssignment;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String memberId() {
        return URLEncoder.encode(memberId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/policies/assignments/members/" +
                URLEncoder.encode(memberId, StandardCharsets.UTF_8);
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
    public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailMemberPolicyAssignmentChangeRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyAssignmentChangeResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailMemberPolicyAssignmentChangeRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailMemberPolicyAssignmentChangeRequest> {

        private final OpenRouterClient client;
        private String memberId;
        private String workspaceId;
        private String policyType;
        private String policyId;
        private Boolean assigned;
        private OpenRouterGuardrailPolicyAssignmentState expectedAssignment;
        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param memberId the organization member user id identifying the subject (URL-encoded into the path segment)
         */
        public Builder(OpenRouterClient client, String memberId) {
            super(client);
            this.client = client;
            this.memberId = memberId;
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
        public OpenRouterGuardrailMemberPolicyAssignmentChangeRequest build() {
            if (memberId == null || memberId.isEmpty()) {
                throw new IllegalStateException("memberId is required");
            }
            if (".".equals(memberId) || "..".equals(memberId)) {
                throw new IllegalStateException("memberId of \".\" or \"..\" cannot be addressed by path");
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
            return new OpenRouterGuardrailMemberPolicyAssignmentChangeRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailMemberPolicyAssignmentChangeRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailMemberPolicyAssignmentChangeRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentChangeResponse<OpenRouterGuardrailMemberPolicyAssignmentChangeRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
