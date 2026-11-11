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
 * A request to read the guardrail-policy assignment of one organization member:
 * 
 * GET https://openrouter.ai/api/v1/guardrails/policies/assignments/members/{member_id}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailMemberPolicyAssignmentGetRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailMemberPolicyAssignmentGetRequest>> {

    private final OpenRouterClient client;
    private final String memberId;
    private final String workspaceId;
    private final String policyType;

    private OpenRouterGuardrailMemberPolicyAssignmentGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.memberId = builder.memberId;
        this.workspaceId = builder.workspaceId;
        this.policyType = builder.policyType;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String memberId() {
        return URLEncoder.encode(memberId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/policies/assignments/members/")
                .append(URLEncoder.encode(memberId, StandardCharsets.UTF_8));
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
    public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailMemberPolicyAssignmentGetRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyAssignmentResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailMemberPolicyAssignmentGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailMemberPolicyAssignmentGetRequest> {

        private final OpenRouterClient client;
        private String memberId;
        private String workspaceId;
        private String policyType;
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
        public OpenRouterGuardrailMemberPolicyAssignmentGetRequest build() {
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
            return new OpenRouterGuardrailMemberPolicyAssignmentGetRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailMemberPolicyAssignmentGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailMemberPolicyAssignmentGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyAssignmentResponse<OpenRouterGuardrailMemberPolicyAssignmentGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
