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
 * A request to create one model-availability guardrail policy in a workspace:
 * POST https://openrouter.ai/api/v1/guardrails/policies/model-availability
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailModelAvailabilityPolicyCreateRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyCreateRequest>> {

    private final OpenRouterClient client;
    private final String name;
    private final OpenRouterGuardrailModelAvailabilityPolicyConfig config;
    private final String workspaceId;
    private final List<String> apiKeyHashes;
    private final List<String> memberIds;
    private final Boolean isWorkspaceDefault;
    private final List<String> optedOutApiKeyHashes;
    private final List<String> optedOutMemberIds;

    private OpenRouterGuardrailModelAvailabilityPolicyCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
        this.config = builder.config;
        this.workspaceId = builder.workspaceId;
        this.apiKeyHashes = builder.apiKeyHashes == null ? null : List.copyOf(builder.apiKeyHashes);
        this.memberIds = builder.memberIds == null ? null : List.copyOf(builder.memberIds);
        this.isWorkspaceDefault = builder.isWorkspaceDefault;
        this.optedOutApiKeyHashes = builder.optedOutApiKeyHashes == null ? null : List.copyOf(builder.optedOutApiKeyHashes);
        this.optedOutMemberIds = builder.optedOutMemberIds == null ? null : List.copyOf(builder.optedOutMemberIds);
    }

    /** @return the policy name as sent */
    public String name() {
        return name;
    }

    /** @return the workspace the policy belongs to */
    public String workspaceId() {
        return workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/policies/model-availability";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("policy_type", "model_availability");
        body.put("config", config.toJson());
        body.put("workspace_id", workspaceId);
        if (apiKeyHashes != null) {
            body.put("api_key_hashes", new JSONArray(apiKeyHashes));
        }
        if (memberIds != null) {
            body.put("member_ids", new JSONArray(memberIds));
        }
        if (isWorkspaceDefault != null) {
            body.put("is_workspace_default", isWorkspaceDefault.booleanValue());
        }
        if (optedOutApiKeyHashes != null) {
            body.put("opted_out_api_key_hashes", new JSONArray(optedOutApiKeyHashes));
        }
        if (optedOutMemberIds != null) {
            body.put("opted_out_member_ids", new JSONArray(optedOutMemberIds));
        }
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyCreateRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailModelAvailabilityPolicyCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailModelAvailabilityPolicyCreateRequest> {

        private final OpenRouterClient client;
        private String name;
        private OpenRouterGuardrailModelAvailabilityPolicyConfig config;
        private String workspaceId;
        private List<String> apiKeyHashes;
        private List<String> memberIds;
        private Boolean isWorkspaceDefault;
        private List<String> optedOutApiKeyHashes;
        private List<String> optedOutMemberIds;

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
         * Sets the body field {@code name} (required): the policy name
         * (1-255 characters).
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the body field {@code config} (required): the model-availability
         * authoring config ({@link OpenRouterGuardrailModelAvailabilityPolicyConfig}).
         */
        public Builder config(OpenRouterGuardrailModelAvailabilityPolicyConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets the body field {@code workspace_id} (required): the
         * workspace the policy belongs to.
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the body field {@code api_key_hashes}: the API key hashes
         * the policy is assigned to. A set list replaces the previous one.
         */
        public Builder apiKeyHashes(List<String> apiKeyHashes) {
            this.apiKeyHashes = apiKeyHashes;
            return this;
        }

        /**
         * Adds one API key hash to {@code api_key_hashes} (accumulates).
         */
        public Builder addApiKeyHash(String apiKeyHash) {
            if (apiKeyHash == null) {
                throw new IllegalArgumentException("apiKeyHash must not be null");
            }
            if (this.apiKeyHashes == null) {
                this.apiKeyHashes = new ArrayList<>();
            }
            this.apiKeyHashes.add(apiKeyHash);
            return this;
        }

        /**
         * Sets the body field {@code member_ids}: the organization member
         * user ids the policy is assigned to. A set list replaces the
         * previous one.
         */
        public Builder memberIds(List<String> memberIds) {
            this.memberIds = memberIds;
            return this;
        }

        /**
         * Adds one organization member user id to {@code member_ids}
         * (accumulates).
         */
        public Builder addMemberId(String memberId) {
            if (memberId == null) {
                throw new IllegalArgumentException("memberId must not be null");
            }
            if (this.memberIds == null) {
                this.memberIds = new ArrayList<>();
            }
            this.memberIds.add(memberId);
            return this;
        }

        /**
         * Sets the body field {@code is_workspace_default}: make this the
         * workspace default for its policy type (the API default is
         * {@code false}). {@code false} is a set option and is emitted;
         * {@code null} omits the field.
         */
        public Builder isWorkspaceDefault(Boolean isWorkspaceDefault) {
            this.isWorkspaceDefault = isWorkspaceDefault;
            return this;
        }

        /**
         * Sets the body field {@code opted_out_api_key_hashes}: API keys
         * that should not inherit the policy when it is the workspace
         * default.
         */
        public Builder optedOutApiKeyHashes(List<String> optedOutApiKeyHashes) {
            this.optedOutApiKeyHashes = optedOutApiKeyHashes;
            return this;
        }

        /**
         * Sets the body field {@code opted_out_member_ids}: members that
         * should not inherit the policy when it is the workspace default.
         */
        public Builder optedOutMemberIds(List<String> optedOutMemberIds) {
            this.optedOutMemberIds = optedOutMemberIds;
            return this;
        }

        @Override
        public OpenRouterGuardrailModelAvailabilityPolicyCreateRequest build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("name is required");
            }
            if (name.codePointCount(0, name.length()) > 255) {
                throw new IllegalStateException("name must be at most 255 characters");
            }
            if (config == null) {
                throw new IllegalStateException("config is required");
            }
            if (workspaceId == null || workspaceId.isEmpty()) {
                throw new IllegalStateException("workspace_id is required");
            }
            return new OpenRouterGuardrailModelAvailabilityPolicyCreateRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
